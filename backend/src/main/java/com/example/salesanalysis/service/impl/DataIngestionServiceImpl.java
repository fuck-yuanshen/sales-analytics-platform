package com.example.salesanalysis.service.impl;

import com.example.salesanalysis.domain.*;
import com.example.salesanalysis.dto.SyncRequest;
import com.example.salesanalysis.dto.UploadResultResponse;
import com.example.salesanalysis.enums.SyncFrequency;
import com.example.salesanalysis.mapper.*;
import com.example.salesanalysis.service.DataIngestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataIngestionServiceImpl implements DataIngestionService {

    private static final String RETENTION_DAYS_KEY = "retention_days";
    private static final String SYNC_FREQUENCY_KEY = "sync_frequency";
    private static final String LAST_AUTO_SYNC_AT_KEY = "last_auto_sync_at";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter CONFIG_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final BackupRecordMapper backupRecordMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private final Path backupDir = Paths.get("backups");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(backupDir);
        if (systemConfigMapper.getValue(RETENTION_DAYS_KEY) == null) {
            systemConfigMapper.insert(RETENTION_DAYS_KEY, "180");
        }
        if (systemConfigMapper.getValue(SYNC_FREQUENCY_KEY) == null) {
            systemConfigMapper.insert(SYNC_FREQUENCY_KEY, SyncFrequency.DAILY.name());
        }
    }

    @Override
    @Transactional
    public UploadResultResponse importOfflineFile(MultipartFile file) throws IOException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        int success = 0;
        int failed = 0;
        if (fileName.endsWith(".csv")) {
            try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                try {
                    String[] row;
                    boolean firstRow = true;
                    while ((row = reader.readNext()) != null) {
                        if (firstRow) {
                            firstRow = false;
                            continue;
                        }
                        try {
                            upsertFromFlatRow(row);
                            success++;
                        } catch (Exception ex) {
                            failed++;
                        }
                    }
                } catch (CsvValidationException ex) {
                    throw new IOException("CSV内容格式不合法: " + ex.getMessage(), ex);
                }
            }
        } else if (fileName.endsWith(".xlsx")) {
            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                boolean firstRow = true;
                for (Row r : sheet) {
                    if (firstRow) {
                        firstRow = false;
                        continue;
                    }
                    try {
                        String[] row = new String[20];
                        for (int i = 0; i < 20; i++) {
                            row[i] = r.getCell(i) == null ? "" : r.getCell(i).toString();
                        }
                        upsertFromFlatRow(row);
                        success++;
                    } catch (Exception ex) {
                        failed++;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("仅支持 .csv 和 .xlsx 文件");
        }
        return new UploadResultResponse(success, failed, "导入完成");
    }

    @Override
    @Transactional
    public String syncFromExternal(SyncRequest request) {
        int imported = 0;
        try (Connection conn = DriverManager.getConnection(request.getJdbcUrl(), request.getUsername(), request.getPassword())) {
            String sql = "SELECT order_no, user_code, user_name, gender, province, city, district, user_tag, spending_tier, " +
                    "order_type, payment_status, placed_at, paid_at, total_amount, total_quantity, sku, product_name, category, unit_price, item_quantity, item_amount " +
                    "FROM external_sales_data";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] row = new String[]{
                            rs.getString("order_no"), rs.getString("user_code"), rs.getString("user_name"), rs.getString("gender"),
                            rs.getString("province"), rs.getString("city"), rs.getString("district"), rs.getString("user_tag"),
                            rs.getString("spending_tier"), rs.getString("order_type"), rs.getString("payment_status"),
                            formatDate(rs.getTimestamp("placed_at")), formatDate(rs.getTimestamp("paid_at")),
                            rs.getBigDecimal("total_amount").toPlainString(), String.valueOf(rs.getInt("total_quantity")),
                            rs.getString("sku"), rs.getString("product_name"), rs.getString("category"),
                            rs.getBigDecimal("unit_price").toPlainString(), String.valueOf(rs.getInt("item_quantity")),
                            rs.getBigDecimal("item_amount").toPlainString()
                    };
                    upsertFromFlatRow(row);
                    imported++;
                }
            }
            return "外部同步完成，导入数据行数: " + imported;
        } catch (Exception ex) {
            int generated = generateMockOrders(30);
            return "外部数据源不可用，已使用模拟数据兜底同步，新增行数: " + generated + "，原因: " + ex.getMessage();
        }
    }

    @Override
    @Transactional
    public String runScheduledSync() {
        int inserted = generateMockOrders(15);
        return "定时同步完成，新增行数: " + inserted;
    }

    @Override
    @Transactional
    public String runAutoScheduledSync() {
        SyncFrequency frequency = SyncFrequency.valueOf(getSyncFrequency());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSyncAt = readLastAutoSyncAt();
        if (!shouldRunAutoSync(frequency, lastSyncAt, now)) {
            return "自动同步已跳过，频率=" + frequency + "，上次同步时间=" + (lastSyncAt == null ? "无" : lastSyncAt);
        }
        int inserted = generateMockOrders(15);
        upsertConfig(LAST_AUTO_SYNC_AT_KEY, now.format(CONFIG_TIME_FORMATTER));
        return "自动同步已执行，频率=" + frequency + "，新增行数: " + inserted;
    }

    @Override
    @Transactional
    public int archiveExpiredData() {
        Integer retentionDays = getRetentionDays();
        LocalDateTime archiveBefore = LocalDateTime.now().minusDays(retentionDays);
        List<OrderRecord> orders = orderMapper.findArchivable(archiveBefore);
        if (orders.isEmpty()) {
            return 0;
        }
        List<Long> orderIds = orders.stream().map(OrderRecord::getId).toList();
        List<OrderItem> items = orderItemMapper.findByOrderIds(orderIds);

        for (OrderRecord order : orders) {
            orderMapper.archive(order);
        }
        for (OrderItem item : items) {
            orderItemMapper.archive(item);
        }
        orderItemMapper.deleteByOrderIds(orderIds);
        orderMapper.deleteByIds(orderIds);
        return orders.size();
    }

    @Override
    @Transactional
    public BackupRecord backupData(String comment) throws IOException {
        String fileName = "backup-" + System.currentTimeMillis() + ".json";
        Path filePath = backupDir.resolve(fileName);

        Map<String, Object> payload = new HashMap<>();
        payload.put("users", jdbcTemplate.queryForList("SELECT * FROM users"));
        payload.put("products", jdbcTemplate.queryForList("SELECT * FROM products"));
        payload.put("orders", jdbcTemplate.queryForList("SELECT * FROM orders"));
        payload.put("order_items", jdbcTemplate.queryForList("SELECT * FROM order_items"));

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), payload);

        BackupRecord record = new BackupRecord();
        record.setFileName(fileName);
        record.setFilePath(filePath.toAbsolutePath().toString());
        record.setComment(comment);
        backupRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public void restoreBackup(Long backupId) throws IOException {
        BackupRecord record = listBackups().stream()
                .filter(it -> Objects.equals(it.getId(), backupId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到对应备份记录"));

        Map<String, List<Map<String, Object>>> payload = objectMapper.readValue(
                new File(record.getFilePath()),
                new TypeReference<>() {
                }
        );

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM products");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        List<Map<String, Object>> users = payload.getOrDefault("users", Collections.emptyList());
        for (Map<String, Object> row : users) {
            jdbcTemplate.update("INSERT INTO users(id, user_code, user_name, gender, province, city, district, user_tag, spending_tier, created_at) VALUES(?,?,?,?,?,?,?,?,?,?)",
                    row.get("id"), row.get("user_code"), row.get("user_name"), row.get("gender"), row.get("province"), row.get("city"),
                    row.get("district"), row.get("user_tag"), row.get("spending_tier"), row.get("created_at"));
        }

        List<Map<String, Object>> products = payload.getOrDefault("products", Collections.emptyList());
        for (Map<String, Object> row : products) {
            jdbcTemplate.update("INSERT INTO products(id, sku, product_name, category, unit_price, created_at) VALUES(?,?,?,?,?,?)",
                    row.get("id"), row.get("sku"), row.get("product_name"), row.get("category"), row.get("unit_price"), row.get("created_at"));
        }

        List<Map<String, Object>> orders = payload.getOrDefault("orders", Collections.emptyList());
        for (Map<String, Object> row : orders) {
            jdbcTemplate.update("INSERT INTO orders(id, order_no, user_id, order_type, payment_status, placed_at, paid_at, total_amount, total_quantity, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    row.get("id"), row.get("order_no"), row.get("user_id"), row.get("order_type"), row.get("payment_status"), row.get("placed_at"), row.get("paid_at"),
                    row.get("total_amount"), row.get("total_quantity"), row.get("created_at"), row.get("updated_at"));
        }

        List<Map<String, Object>> orderItems = payload.getOrDefault("order_items", Collections.emptyList());
        for (Map<String, Object> row : orderItems) {
            jdbcTemplate.update("INSERT INTO order_items(id, order_id, product_id, quantity, unit_price, amount) VALUES(?,?,?,?,?,?)",
                    row.get("id"), row.get("order_id"), row.get("product_id"), row.get("quantity"), row.get("unit_price"), row.get("amount"));
        }
    }

    @Override
    public List<BackupRecord> listBackups() {
        return backupRecordMapper.findAll();
    }

    @Override
    public void updateRetentionDays(Integer retentionDays) {
        upsertConfig(RETENTION_DAYS_KEY, String.valueOf(retentionDays));
    }

    @Override
    public Integer getRetentionDays() {
        String value = systemConfigMapper.getValue(RETENTION_DAYS_KEY);
        return value == null ? 180 : Integer.parseInt(value);
    }

    @Override
    public void updateSyncFrequency(String frequency) {
        SyncFrequency normalized = SyncFrequency.valueOf(frequency.toUpperCase());
        upsertConfig(SYNC_FREQUENCY_KEY, normalized.name());
    }

    @Override
    public String getSyncFrequency() {
        String value = systemConfigMapper.getValue(SYNC_FREQUENCY_KEY);
        if (value == null || value.isBlank()) {
            return SyncFrequency.DAILY.name();
        }
        try {
            return SyncFrequency.valueOf(value.toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            return SyncFrequency.DAILY.name();
        }
    }

    private void upsertFromFlatRow(String[] row) {
        if (row.length < 20) {
            throw new IllegalArgumentException("数据行列数不足，至少需要20列");
        }

        UserProfile user = userMapper.findByUserCode(row[1]);
        if (user == null) {
            user = new UserProfile();
            user.setUserCode(row[1]);
            user.setUserName(row[2]);
            user.setGender(row[3]);
            user.setProvince(row[4]);
            user.setCity(row[5]);
            user.setDistrict(row[6]);
            user.setUserTag(row[7]);
            user.setSpendingTier(row[8]);
            userMapper.insert(user);
        }

        Product product = productMapper.findBySku(row[15]);
        if (product == null) {
            product = new Product();
            product.setSku(row[15]);
            product.setProductName(row[16]);
            product.setCategory(row[17]);
            product.setUnitPrice(parseDecimal(row[18]));
            productMapper.insert(product);
        }

        List<OrderRecord> existingOrders = orderMapper.findByOrderNos(List.of(row[0]));
        OrderRecord order;
        if (existingOrders.isEmpty()) {
            order = new OrderRecord();
            order.setOrderNo(row[0]);
            order.setUserId(user.getId());
            order.setOrderType(defaultIfBlank(row[9], "NORMAL"));
            order.setPaymentStatus(defaultIfBlank(row[10], "PAID"));
            order.setPlacedAt(parseDate(row[11]));
            order.setPaidAt(parseDateNullable(row[12]));
            order.setTotalAmount(parseDecimal(row[13]));
            order.setTotalQuantity(parseInteger(row[14]));
            orderMapper.insert(order);
        } else {
            order = existingOrders.get(0);
        }

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setQuantity(parseInteger(row[19]));
        item.setUnitPrice(parseDecimal(row[18]));
        item.setAmount(row.length > 20 ? parseDecimal(row[20]) : parseDecimal(row[18]).multiply(BigDecimal.valueOf(item.getQuantity())));
        orderItemMapper.insert(item);
    }

    private int generateMockOrders(int rows) {
        String[] provinces = {"Guangdong", "Jiangsu", "Zhejiang", "Sichuan", "Beijing"};
        String[] cities = {"Guangzhou", "Shenzhen", "Nanjing", "Hangzhou", "Chengdu", "Beijing"};
        String[] tags = {"NEW", "RETURNING"};
        String[] tiers = {"L1", "L2", "L3"};
        String[] categories = {"Digital", "Home", "Beauty", "Food"};
        Random random = new Random();

        int inserted = 0;
        for (int i = 0; i < rows; i++) {
            String userCode = "U" + (1000 + random.nextInt(200));
            String sku = "SKU-" + (100 + random.nextInt(80));
            String orderNo = "ORD" + System.currentTimeMillis() + random.nextInt(1000);
            String province = provinces[random.nextInt(provinces.length)];
            String city = cities[random.nextInt(cities.length)];
            String[] row = new String[]{
                    orderNo,
                    userCode,
                    "User-" + userCode,
                    random.nextBoolean() ? "M" : "F",
                    province,
                    city,
                    city + "-District",
                    tags[random.nextInt(tags.length)],
                    tiers[random.nextInt(tiers.length)],
                    random.nextBoolean() ? "NORMAL" : "GROUP",
                    random.nextInt(10) < 8 ? "PAID" : "PLACED",
                    LocalDateTime.now().minusDays(random.nextInt(60)).format(DATETIME_FORMATTER),
                    LocalDateTime.now().minusDays(random.nextInt(60)).format(DATETIME_FORMATTER),
                    String.valueOf(50 + random.nextInt(500)),
                    String.valueOf(1 + random.nextInt(8)),
                    sku,
                    "Product-" + sku,
                    categories[random.nextInt(categories.length)],
                    String.valueOf(20 + random.nextInt(120)),
                    String.valueOf(1 + random.nextInt(5)),
                    String.valueOf(30 + random.nextInt(350))
            };
            upsertFromFlatRow(row);
            inserted++;
        }
        return inserted;
    }

    private boolean shouldRunAutoSync(SyncFrequency frequency, LocalDateTime lastSyncAt, LocalDateTime now) {
        if (lastSyncAt == null) {
            return true;
        }
        return switch (frequency) {
            case HOURLY -> !lastSyncAt.plusHours(1).isAfter(now);
            case DAILY -> !lastSyncAt.toLocalDate().isEqual(now.toLocalDate());
            case WEEKLY -> !lastSyncAt.plusWeeks(1).isAfter(now);
        };
    }

    private LocalDateTime readLastAutoSyncAt() {
        String value = systemConfigMapper.getValue(LAST_AUTO_SYNC_AT_KEY);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, CONFIG_TIME_FORMATTER);
        } catch (Exception ex) {
            return null;
        }
    }

    private void upsertConfig(String key, String value) {
        if (systemConfigMapper.getValue(key) == null) {
            systemConfigMapper.insert(key, value);
            return;
        }
        systemConfigMapper.update(key, value);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return new BigDecimal(value.trim()).intValue();
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(value.trim(), DATETIME_FORMATTER);
    }

    private LocalDateTime parseDateNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), DATETIME_FORMATTER);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String formatDate(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        return ts.toLocalDateTime().format(DATETIME_FORMATTER);
    }
}

