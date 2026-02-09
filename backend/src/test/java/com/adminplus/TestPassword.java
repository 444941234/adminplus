package com.adminplus;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        
        // 生成新的编码
        String newEncoded = encoder.encode(rawPassword);
        System.out.println("New encoded password: " + newEncoded);
        System.out.println("Length: " + newEncoded.length());
        
        // 测试原始密码
        String originalEncoded = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH";
        System.out.println("Original matches: " + encoder.matches(rawPassword, originalEncoded));
    }
}
