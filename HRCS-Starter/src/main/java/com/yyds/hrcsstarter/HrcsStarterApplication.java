package com.yyds.hrcsstarter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@MapperScan(value = "com.yyds.hrcsserver.**.mapper")
public class HrcsStarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrcsStarterApplication.class, args);
    }

}
