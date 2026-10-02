# 销售数据分析系统（交付版）

本项目为毕业设计实现，技术栈如下：

- 后端：Spring Boot + MyBatis + Quartz + MySQL
- 前端：Vue 2 + Element UI + ECharts
- 部署：支持本地运行，支持 Docker（可选）

## 功能概览

### 1. 数据接入与治理
- 外部数据源同步（MySQL/Oracle JDBC）
- 定时同步，频率可配置：`HOURLY` / `DAILY` / `WEEKLY`
- 离线导入：CSV/XLSX
- 数据保留策略与历史归档
- 数据备份与恢复

### 2. 多维销售分析
- 多维筛选：时间、地区、支付状态、订单类型、用户标签、消费层级
- 核心指标：销售总额、环比、同比、销量、客单价、复购率、转化率
- 下钻分析：省 -> 市 -> 区

### 3. 可视化与交互
- 销售趋势、商品 Top10、用户标签分布、区域热力排行
- 图表交互：tooltip、legend、dataZoom、点击下钻
- 图表导出：PNG/JPG
- 报表导出：Excel/PDF

### 4. 预警能力
- 预警规则增删改查
- 定时评估与事件记录
- 预警中心展示

## 运行说明

### 1）准备数据库

```bash
mysql -u<用户名> -p -e "CREATE DATABASE IF NOT EXISTS sales_analysis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

说明：
- 数据库账号和密码请通过本地环境变量或未纳入版本控制的配置文件提供。

### 2）启动后端

```bash
cd backend
mvn -DskipTests package
java -jar target/sales-analysis-backend-0.0.1-SNAPSHOT.jar
```

后端地址：`http://localhost:8080`

### 3）启动前端

```bash
cd frontend
npm install
npm run serve
```

前端地址：`http://localhost:5173`

### 4）可选：生成模拟数据

```bash
python mock_data/generate_mock_sales_data.py
```

生成后可在“数据接入与治理”页面导入 `mock_data/output/mock_sales_data.csv`。
更多参数说明见 `mock_data/README.md`。

## Docker 运行（可选）

```bash
docker compose up -d --build
```

服务端口：
- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
- MySQL：`localhost:3306`

## 关键接口

### 分析接口
- `POST /api/analytics/summary`
- `POST /api/analytics/trend`
- `POST /api/analytics/top-products`
- `POST /api/analytics/user-distribution/gender`
- `POST /api/analytics/user-distribution/tag`
- `POST /api/analytics/geo/{level}`
- `POST /api/analytics/drill-down`

### 数据接入与治理
- `POST /api/data-ingestion/upload`
- `POST /api/data-ingestion/sync`
- `POST /api/data-ingestion/sync/scheduled`
- `POST /api/data-ingestion/archive`
- `POST /api/data-ingestion/backup`
- `POST /api/data-ingestion/restore/{backupId}`
- `GET /api/data-ingestion/backups`
- `GET /api/data-ingestion/retention`
- `PUT /api/data-ingestion/retention`
- `GET /api/data-ingestion/sync-frequency`
- `PUT /api/data-ingestion/sync-frequency`

### 预警接口
- `GET /api/alerts/rules`
- `POST /api/alerts/rules`
- `PUT /api/alerts/rules/{id}`
- `DELETE /api/alerts/rules/{id}`
- `GET /api/alerts/events`
- `POST /api/alerts/evaluate`

### 模板与报表
- `GET /api/chart-templates`
- `POST /api/chart-templates`
- `PUT /api/chart-templates/{id}`
- `DELETE /api/chart-templates/{id}`
- `POST /api/chart-templates/recommend`
- `POST /api/reports/export`

## 注意事项

- 已按要求暂不启用 Redis，当前配置为 `spring.cache.type=simple`。
- `开题报告.md` 曾发生历史编码损坏，当前仓库已提供可读的中文验收文档：
  - `DELIVERY_CHECKLIST.md`
  - `开题报告_需求验收说明.md`

