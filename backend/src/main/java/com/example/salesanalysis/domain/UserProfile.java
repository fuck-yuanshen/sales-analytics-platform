package com.example.salesanalysis.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfile {
    private Long id;
    private String userCode;
    private String userName;
    private String gender;
    private String province;
    private String city;
    private String district;
    private String userTag;
    private String spendingTier;
    private LocalDateTime createdAt;
}
