package com.starserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.star")
@MapperScan("com.star.mapper")
@EnableScheduling
public class StarServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarServerApplication.class, args);
    }

}
