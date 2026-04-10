package com.adminplus.utils.masking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 脱敏器责任链
 * <p>
 * 按优先级顺序执行脱敏规则
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class LogMaskerChain implements LogMasker {

    private final LogMasker[] maskers;

    private LogMaskerChain(LogMasker[] maskers) {
        this.maskers = maskers;
    }

    @Override
    public String mask(String input) {
        String result = input;
        for (LogMasker masker : maskers) {
            result = masker.mask(result);
        }
        return result;
    }

    /**
     * 创建默认脱敏规则链
     */
    public static LogMaskerChain createDefault() {
        List<LogMasker> maskers = new ArrayList<>();

        // 按优先级添加内置规则
        maskers.add(new PasswordMasker());
        maskers.add(new TokenMasker());
        maskers.add(new IdCardMasker());
        maskers.add(new PhoneMasker());
        maskers.add(new EmailMasker());
        maskers.add(new SqlPasswordMasker());
        maskers.add(new CreditCardMasker());
        maskers.add(new UsernameMasker());
        maskers.add(new IpMasker());

        // 按优先级排序
        maskers.sort(Comparator.comparingInt(m ->
            m instanceof Prioritized p ? p.getPriority() : 1000
        ));

        return new LogMaskerChain(maskers.toArray(new LogMasker[0]));
    }

    /**
     * 创建自定义脱敏规则链
     */
    public static LogMaskerChain of(LogMasker... maskers) {
        return new LogMaskerChain(maskers);
    }

    /**
     * 创建包含额外规则的脱敏链
     *
     * @param additionalMaskers 额外的脱敏器
     * @return 新的责任链
     */
    public LogMaskerChain withAdditional(LogMasker... additionalMaskers) {
        List<LogMasker> all = new ArrayList<>();
        for (LogMasker m : maskers) {
            all.add(m);
        }
        for (LogMasker m : additionalMaskers) {
            all.add(m);
        }
        all.sort(Comparator.comparingInt(m ->
            m instanceof Prioritized p ? p.getPriority() : 1000
        ));
        return new LogMaskerChain(all.toArray(new LogMasker[0]));
    }
}
