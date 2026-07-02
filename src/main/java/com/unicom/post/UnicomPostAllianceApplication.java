package com.unicom.post;

import com.unicom.post.config.CustomBeanNameGenerator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(
        basePackages = {"com.unicom.post.modules.**.mapper"},
        nameGenerator = CustomBeanNameGenerator.class
)
public class UnicomPostAllianceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnicomPostAllianceApplication.class, args);
    }
}