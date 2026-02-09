package com.adminplus.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 雪花ID生成测试
 * 
 * @author AdminPlus
 * @since 2026-02-09
 */
@Component
public class SnowflakeTest implements CommandLineRunner {
    
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    
    public SnowflakeTest(SnowflakeIdGenerator snowflakeIdGenerator) {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 雪花ID生成测试 ===");
        
        // 生成10个ID进行测试
        for (int i = 0; i < 10; i++) {
            String id = snowflakeIdGenerator.nextId();
            System.out.println("生成的雪花ID: " + id + " (长度: " + id.length() + ")");
        }
        
        System.out.println("=== 测试完成 ===");
    }
}