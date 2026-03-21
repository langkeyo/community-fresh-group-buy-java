package com.langkeyo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 社区生鲜团购后端启动类
 * @author langkeyo
 */
@SpringBootApplication
@MapperScan("com.langkeyo.mapper")
public class CommunityFreshGroupBuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityFreshGroupBuyApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   社区生鲜团购后端启动成功！");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("   API文档: http://localhost:8080/doc.html");
        System.out.println("========================================\n");
    }
}