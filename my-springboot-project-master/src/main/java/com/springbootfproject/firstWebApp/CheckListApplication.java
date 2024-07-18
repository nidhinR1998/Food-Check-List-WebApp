package com.springbootfproject.firstWebApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheckListApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CheckListApplication.class, args);
    }

}
