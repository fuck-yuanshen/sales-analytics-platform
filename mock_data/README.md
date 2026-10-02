# 模拟数据脚本说明

本目录用于存放“数据模拟与测试”相关文件。

## 1. 脚本文件

- `generate_mock_sales_data.py`：生成可导入系统的销售明细 CSV

## 2. 使用方式

在项目根目录执行：

```bash
python mock_data/generate_mock_sales_data.py
```

可选参数：

```bash
python mock_data/generate_mock_sales_data.py --orders 500 --max-items-per-order 4 --days-back 120 --seed 42 --output mock_data/output/mock_sales_data.csv
```

参数说明：
- `--orders`：订单数量
- `--max-items-per-order`：每个订单最多商品行数
- `--days-back`：时间回溯天数
- `--seed`：随机种子（可选，便于复现实验数据）
- `--output`：输出文件路径

## 3. 导入系统

1. 启动前后端服务  
2. 打开“数据接入与治理”页面  
3. 在“离线导入（CSV/XLSX）”处上传生成的 CSV 文件  

脚本生成字段已严格匹配系统导入格式，可直接导入。
