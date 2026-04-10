package com.adminplus.runner.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据初始化器抽象基类
 * <p>
 * 提供模板方法，自动处理"数据已存在则跳过"的通用逻辑。
 * 子类只需实现 {@link #doInitialize()} 方法，专注业务逻辑。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Component
 * public class MyInitializer extends AbstractDataInitializer {
 *     private final MyRepository repository;
 *
 *     public MyInitializer(MyRepository repository) {
 *         super(repository, "我的数据");
 *         this.repository = repository;
 *     }
 *
 *     @Override
 *     protected void doInitialize() {
 *         // 这里只写初始化逻辑，不需要检查数据是否存在
 *         repository.saveAll(...);
 *     }
 * }
 * }</pre>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
public abstract class AbstractDataInitializer implements DataInitializer {

    private final SkipCheckable skipChecker;
    private final String dataName;

    /**
     * 构造函数
     *
     * @param skipChecker 数据存在性检查器
     * @param dataName    数据名称（用于日志）
     */
    protected AbstractDataInitializer(SkipCheckable skipChecker, String dataName) {
        this.skipChecker = skipChecker;
        this.dataName = dataName;
    }

    /**
     * 模板方法：执行初始化
     * <p>
     * 自动检查数据是否已存在，如果存在则跳过初始化。
     */
    @Override
    @Transactional
    public final void initialize() {
        if (skipChecker.hasData()) {
            log.info("{}数据已存在，跳过初始化", dataName);
            return;
        }

        log.info("开始初始化{}数据...", dataName);
        long startTime = System.currentTimeMillis();

        try {
            doInitialize();
            long duration = System.currentTimeMillis() - startTime;
            log.info("{}数据初始化完成，耗时 {} ms", dataName, duration);
        } catch (Exception e) {
            log.error("{}数据初始化失败", dataName, e);
            throw e;
        }
    }

    /**
     * 子类实现：执行具体的初始化逻辑
     * <p>
     * 注意：此方法执行时已经确认数据不存在，子类无需重复检查。
     */
    protected abstract void doInitialize();

    /**
     * 数据存在性检查接口
     */
    @FunctionalInterface
    public interface SkipCheckable {
        /**
         * 检查数据是否已存在
         *
         * @return true 如果数据已存在，应跳过初始化
         */
        boolean hasData();
    }
}
