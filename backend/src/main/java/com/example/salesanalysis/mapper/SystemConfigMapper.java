package com.example.salesanalysis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemConfigMapper {

    @Select("SELECT config_value FROM system_config WHERE config_key = #{key}")
    String getValue(@Param("key") String key);

    @Insert("INSERT INTO system_config(config_key, config_value, updated_at) VALUES(#{key}, #{value}, NOW())")
    int insert(@Param("key") String key, @Param("value") String value);

    @Update("UPDATE system_config SET config_value = #{value}, updated_at = NOW() WHERE config_key = #{key}")
    int update(@Param("key") String key, @Param("value") String value);
}
