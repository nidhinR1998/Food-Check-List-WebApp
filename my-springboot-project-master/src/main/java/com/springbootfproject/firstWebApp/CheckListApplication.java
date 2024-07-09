package com.springbootfproject.firstWebApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class CheckListApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CheckListApplication.class, args);
    }

   
}