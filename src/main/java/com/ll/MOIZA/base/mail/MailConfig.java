package com.ll.MOIZA.base.mail;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {
    @Bean
    public SendGrid sendGrid(@Value("${spring.sendgrid.api-key}") String apiKey) {
        return new SendGrid(apiKey);
    }
}
