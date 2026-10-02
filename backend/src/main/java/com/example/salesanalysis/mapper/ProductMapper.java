package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM products WHERE sku = #{sku} LIMIT 1")
    Product findBySku(@Param("sku") String sku);

    @Insert("""
        INSERT INTO products(sku, product_name, category, unit_price, created_at)
        VALUES(#{sku}, #{productName}, #{category}, #{unitPrice}, NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);
}
