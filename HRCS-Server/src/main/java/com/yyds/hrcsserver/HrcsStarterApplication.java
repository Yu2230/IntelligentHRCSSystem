package com.yyds.hrcsserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * 八股
 * springboot：
 *
 */
@SpringBootApplication
@MapperScan(value = "com.yyds.hrcsserver.**.mapper")
@ComponentScan({
        "com.yyds.hrcsserver",        // Controller 和 Service
        "com.yyds.hrcscommon"      // Controller 所在包
})
public class HrcsStarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrcsStarterApplication.class, args);
    }

}
