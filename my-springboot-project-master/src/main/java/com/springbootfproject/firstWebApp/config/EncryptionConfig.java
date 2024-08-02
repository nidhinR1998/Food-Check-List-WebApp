package com.springbootfproject.firstWebApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${encryption.key}")
    private String encryptionKey;

    public String getEncryptionKey() {
        return encryptionKey;
    }
}
