package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE user_code = #{userCode} LIMIT 1")
    UserProfile findByUserCode(@Param("userCode") String userCode);

    @Insert("""
        INSERT INTO users(user_code, user_name, gender, province, city, district, user_tag, spending_tier, created_at)
        VALUES(#{userCode}, #{userName}, #{gender}, #{province}, #{city}, #{district}, #{userTag}, #{spendingTier}, NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProfile userProfile);
}
