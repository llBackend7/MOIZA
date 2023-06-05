package com.ll.MOIZA.base.appConfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Getter
    private static String appKey;

    @Value("${spring.security.oauth2.client.registration.kakao.map-appKey}")
    public void setAppKey(String appKey) {
        AppConfig.appKey = appKey;
    }
}
