package com.ll.MOIZA.base.appConfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;

@Configuration
public class AppConfig {

    @Getter
    private static String appKey;
    @Getter
    private static String baseUrl;
    @Getter
    private static String calculatorUrl;

    @Value("${spring.security.oauth2.client.registration.kakao.map-appKey}")
    public void setAppKey(String appKey) {
        AppConfig.appKey = appKey;
    }

    @Value("${custom.site.baseUrl}")
    public void myPage(String baseUrl) { AppConfig.baseUrl = baseUrl; }

    @Value("${custom.site.calculatorUrl}")
    private void setCalculatorUrl(String calculatorUrl) { AppConfig.calculatorUrl = calculatorUrl; };
}
