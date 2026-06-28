package com.example.creativeworkstation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cloudflare.r2")
public class R2Properties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String publicDomain;
}
