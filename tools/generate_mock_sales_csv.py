#!/usr/bin/env python3
import csv
import random
from datetime import datetime, timedelta

PROVINCES = ["Guangdong", "Zhejiang", "Jiangsu", "Sichuan", "Beijing"]
CITIES = {
    "Guangdong": ["Guangzhou", "Shenzhen"],
    "Zhejiang": ["Hangzhou", "Ningbo"],
    "Jiangsu": ["Nanjing", "Suzhou"],
    "Sichuan": ["Chengdu", "Mianyang"],
    "Beijing": ["Beijing"]
}
CATEGORIES = ["Digital", "Home", "Beauty", "Food"]
USER_TAGS = ["NEW", "RETURNING"]
TIERS = ["L1", "L2", "L3"]

HEADER = [
    "order_no", "user_code", "user_name", "gender", "province", "city", "district", "user_tag", "spending_tier",
    "order_type", "payment_status", "placed_at", "paid_at", "total_amount", "total_quantity", "sku", "product_name", "category",
    "unit_price", "item_quantity", "item_amount"
]

def random_datetime(days=90):
    dt = datetime.now() - timedelta(days=random.randint(0, days), hours=random.randint(0, 23), minutes=random.randint(0, 59))
    return dt.strftime("%Y-%m-%d %H:%M:%S")

def generate_rows(n):
    rows = []
    for i in range(n):
        province = random.choice(PROVINCES)
        city = random.choice(CITIES[province])
        user_code = f"U{1000 + random.randint(1, 500)}"
        sku = f"SKU-{100 + random.randint(1, 300)}"
        qty = random.randint(1, 5)
        unit_price = random.randint(20, 600)
        amount = qty * unit_price
        placed = random_datetime()
        paid = placed if random.random() < 0.85 else ""
        rows.append([
            f"ORD-{datetime.now().strftime('%Y%m%d%H%M%S')}-{i}",
            user_code,
            f"User-{user_code}",
            random.choice(["M", "F"]),
            province,
            city,
            f"{city}-District",
            random.choice(USER_TAGS),
            random.choice(TIERS),
            random.choice(["NORMAL", "GROUP"]),
            "PAID" if paid else "PLACED",
            placed,
            paid,
            amount,
            qty,
            sku,
            f"Product-{sku}",
            random.choice(CATEGORIES),
            unit_price,
            qty,
            amount,
        ])
    return rows


def main():
    out_file = "mock_sales_data.csv"
    rows = generate_rows(500)
    with open(out_file, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(HEADER)
        writer.writerows(rows)
    print(f"generated {len(rows)} rows to {out_file}")

if __name__ == "__main__":
    main()
