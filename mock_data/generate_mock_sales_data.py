#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成可导入本系统的模拟销售数据（CSV）。

输出字段顺序严格匹配后端导入逻辑：
order_no,user_code,user_name,gender,province,city,district,user_tag,spending_tier,
order_type,payment_status,placed_at,paid_at,total_amount,total_quantity,sku,product_name,category,
unit_price,item_quantity,item_amount
"""

from __future__ import annotations

import argparse
import csv
import random
from dataclasses import dataclass
from datetime import datetime, timedelta
from pathlib import Path
from typing import List


HEADER = [
    "order_no",
    "user_code",
    "user_name",
    "gender",
    "province",
    "city",
    "district",
    "user_tag",
    "spending_tier",
    "order_type",
    "payment_status",
    "placed_at",
    "paid_at",
    "total_amount",
    "total_quantity",
    "sku",
    "product_name",
    "category",
    "unit_price",
    "item_quantity",
    "item_amount",
]


REGIONS = {
    "广东省": {
        "广州市": ["天河区", "海珠区", "白云区"],
        "深圳市": ["南山区", "福田区", "宝安区"],
    },
    "浙江省": {
        "杭州市": ["西湖区", "滨江区", "余杭区"],
        "宁波市": ["鄞州区", "海曙区", "江北区"],
    },
    "江苏省": {
        "南京市": ["鼓楼区", "玄武区", "建邺区"],
        "苏州市": ["工业园区", "姑苏区", "吴中区"],
    },
    "四川省": {
        "成都市": ["武侯区", "锦江区", "高新区"],
        "绵阳市": ["涪城区", "游仙区", "安州区"],
    },
    "北京市": {
        "北京市": ["朝阳区", "海淀区", "通州区"],
    },
}


@dataclass(frozen=True)
class Product:
    sku: str
    name: str
    category: str
    base_price: float


PRODUCTS = [
    Product("SKU-1001", "无线耳机", "数码", 299.0),
    Product("SKU-1002", "蓝牙音箱", "数码", 219.0),
    Product("SKU-1003", "智能手表", "数码", 499.0),
    Product("SKU-2001", "电热水壶", "家居", 139.0),
    Product("SKU-2002", "空气炸锅", "家居", 459.0),
    Product("SKU-3001", "洁面乳", "美妆", 89.0),
    Product("SKU-3002", "面膜套装", "美妆", 129.0),
    Product("SKU-4001", "坚果礼盒", "食品", 79.0),
    Product("SKU-4002", "咖啡豆", "食品", 99.0),
]


def fmt_dt(dt: datetime) -> str:
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def rand_dt(days_back: int) -> datetime:
    now = datetime.now()
    return now - timedelta(
        days=random.randint(0, days_back),
        hours=random.randint(0, 23),
        minutes=random.randint(0, 59),
        seconds=random.randint(0, 59),
    )


def pick_region() -> tuple[str, str, str]:
    province = random.choice(list(REGIONS.keys()))
    city = random.choice(list(REGIONS[province].keys()))
    district = random.choice(REGIONS[province][city])
    return province, city, district


def round2(n: float) -> float:
    return round(n + 1e-9, 2)


def generate_rows(order_count: int, max_items_per_order: int, days_back: int) -> List[List[str]]:
    rows: List[List[str]] = []
    ts_prefix = datetime.now().strftime("%Y%m%d%H%M%S")

    for idx in range(1, order_count + 1):
        order_no = f"ORD-{ts_prefix}-{idx:05d}"
        user_code = f"U{100000 + random.randint(1, 900000)}"
        user_name = f"用户{user_code[-4:]}"
        gender = random.choice(["M", "F"])
        province, city, district = pick_region()
        user_tag = random.choices(["NEW", "RETURNING"], weights=[3, 7], k=1)[0]
        spending_tier = random.choices(["L1", "L2", "L3"], weights=[4, 4, 2], k=1)[0]
        order_type = random.choice(["NORMAL", "GROUP"])

        placed_at_dt = rand_dt(days_back)
        paid = random.random() < 0.88
        paid_at_dt = placed_at_dt + timedelta(minutes=random.randint(1, 180)) if paid else None
        payment_status = "PAID" if paid else "PLACED"

        item_count = random.randint(1, max_items_per_order)
        items: list[tuple[Product, int, float, float]] = []
        for _ in range(item_count):
            p = random.choice(PRODUCTS)
            qty = random.randint(1, 4)
            ratio = random.uniform(0.88, 1.18)
            unit_price = round2(p.base_price * ratio)
            item_amount = round2(qty * unit_price)
            items.append((p, qty, unit_price, item_amount))

        total_qty = sum(i[1] for i in items)
        total_amount = round2(sum(i[3] for i in items))

        for p, qty, unit_price, item_amount in items:
            rows.append(
                [
                    order_no,
                    user_code,
                    user_name,
                    gender,
                    province,
                    city,
                    district,
                    user_tag,
                    spending_tier,
                    order_type,
                    payment_status,
                    fmt_dt(placed_at_dt),
                    fmt_dt(paid_at_dt) if paid_at_dt else "",
                    f"{total_amount:.2f}",
                    str(total_qty),
                    p.sku,
                    p.name,
                    p.category,
                    f"{unit_price:.2f}",
                    str(qty),
                    f"{item_amount:.2f}",
                ]
            )
    return rows


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="生成可导入销售系统的模拟CSV数据")
    parser.add_argument("--orders", type=int, default=300, help="订单数量（默认300）")
    parser.add_argument(
        "--max-items-per-order",
        type=int,
        default=3,
        help="每个订单最大商品行数（默认3）",
    )
    parser.add_argument("--days-back", type=int, default=90, help="数据时间回溯天数（默认90）")
    parser.add_argument("--seed", type=int, default=None, help="随机种子（可选）")
    parser.add_argument(
        "--output",
        type=str,
        default="mock_data/output/mock_sales_data.csv",
        help="输出CSV路径",
    )
    return parser


def main() -> None:
    args = build_parser().parse_args()
    if args.orders <= 0:
        raise ValueError("--orders 必须大于0")
    if args.max_items_per_order <= 0:
        raise ValueError("--max-items-per-order 必须大于0")
    if args.days_back < 0:
        raise ValueError("--days-back 不能小于0")

    if args.seed is not None:
        random.seed(args.seed)

    rows = generate_rows(
        order_count=args.orders,
        max_items_per_order=args.max_items_per_order,
        days_back=args.days_back,
    )

    out_path = Path(args.output)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with out_path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(HEADER)
        writer.writerows(rows)

    print(f"已生成 {len(rows)} 行商品明细数据")
    print(f"输出文件：{out_path.resolve()}")
    print("可直接在系统“数据接入与治理 -> 离线导入（CSV/XLSX）”中上传该文件。")


if __name__ == "__main__":
    main()

