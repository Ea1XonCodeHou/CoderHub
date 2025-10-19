package com.eaxon.coderhubcommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "coder.jwt")
@Data
public class JwtProperties {
    //用户端
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

    //后台管理端
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;
}
