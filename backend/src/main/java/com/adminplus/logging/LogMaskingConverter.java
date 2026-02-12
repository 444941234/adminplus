package com.adminplus.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import com.adminplus.utils.LogMaskingUtils;

public class LogMaskingConverter extends MessageConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String originalMessage = super.convert(event);
        return LogMaskingUtils.mask(originalMessage);
    }
}
