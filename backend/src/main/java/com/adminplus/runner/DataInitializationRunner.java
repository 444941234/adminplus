package com.adminplus.runner;

import com.adminplus.runner.initializer.DataInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 数据初始化服务
 * <p>
 * 在应用启动时自动执行基础数据初始化。
 * 使用策略模式，各初始化器独立实现 {@link DataInitializer} 接口。
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializationRunner implements CommandLineRunner {

    private final List<DataInitializer> dataInitializers;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始执行数据初始化...");

        try {
            // 按顺序执行所有初始化器
            dataInitializers.stream()
                    .sorted(Comparator.comparingInt(DataInitializer::getOrder))
                    .forEach(initializer -> {
                        try {
                            log.info("执行初始化器: {}", initializer.getName());
                            initializer.initialize();
                        } catch (Exception e) {
                            log.error("初始化器 {} 执行失败", initializer.getName(), e);
                            throw new RuntimeException("初始化器 " + initializer.getName() + " 执行失败", e);
                        }
                    });

            log.info("数据初始化完成！");

        } catch (Exception e) {
            log.error("数据初始化失败", e);
            throw e;
        }
    }
}