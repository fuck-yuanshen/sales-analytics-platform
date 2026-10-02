package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.ChartTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChartTemplateMapper {

    @Select("SELECT * FROM chart_templates ORDER BY id DESC")
    List<ChartTemplate> findAll();

    @Select("SELECT * FROM chart_templates WHERE id = #{id}")
    ChartTemplate findById(@Param("id") Long id);

    @Insert("""
        INSERT INTO chart_templates(template_name, chart_type, title, color_scheme, axis_config, data_format, created_by, created_at, updated_at)
        VALUES(#{templateName}, #{chartType}, #{title}, #{colorScheme}, #{axisConfig}, #{dataFormat}, #{createdBy}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChartTemplate template);

    @Update("""
        UPDATE chart_templates
        SET template_name=#{templateName}, chart_type=#{chartType}, title=#{title}, color_scheme=#{colorScheme}, axis_config=#{axisConfig},
            data_format=#{dataFormat}, created_by=#{createdBy}, updated_at=NOW()
        WHERE id=#{id}
        """)
    int update(ChartTemplate template);

    @Delete("DELETE FROM chart_templates WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
