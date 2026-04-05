package com.adminplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * AdminPlus 主启动类
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AdminPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminPlusApplication.class, args);
        System.out.println("""
            ========================================
              AdminPlus 启动成功！
              访问地址: http://localhost:8081/api
              API 文档: http://localhost:8081/api/swagger-ui.html
              健康检查: http://localhost:8081/api/actuator/health
            ========================================
            """);
    }
}