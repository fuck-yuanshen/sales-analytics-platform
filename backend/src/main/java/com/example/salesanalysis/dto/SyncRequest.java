package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SyncRequest {
    @NotBlank(message = "数据源类型不能为空")
    private String sourceType;

    @NotBlank(message = "JDBC URL不能为空")
    private String jdbcUrl;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String schemaName;
}

