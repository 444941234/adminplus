package com.adminplus.util;

import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * 雪花ID生成器
 * 结构：
 * 1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
 * 
 * @author AdminPlus
 * @since 2026-02-09
 */
@Component
public class SnowflakeIdGenerator {
    
    // 起始时间戳 (2026-02-09 00:00:00)
    private static final long EPOCH = 1707494400000L;
    
    // 机器ID位数
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    
    // 最大机器ID
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    
    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;
    
    // 移位
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    // 序列号掩码
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    /**
     * 构造函数，自动生成机器ID和数据中心ID
     */
    public SnowflakeIdGenerator() {
        this.datacenterId = getDatacenterId();
        this.workerId = getWorkerId(datacenterId);
    }
    
    /**
     * 生成雪花ID
     */
    public synchronized String nextId() {
        long timestamp = timeGen();
        
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", 
                    lastTimestamp - timestamp));
        }
        
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        return String.valueOf(((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
                (datacenterId << DATACENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence);
    }
    
    /**
     * 获取下一个时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    /**
     * 获取当前时间戳
     */
    private long timeGen() {
        return Instant.now().toEpochMilli();
    }
    
    /**
     * 获取数据中心ID
     */
    private long getDatacenterId() {
        long id = 0L;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    id = ((0x000000FF & (long) mac[mac.length - 1]) |
                          (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    id = id % (MAX_DATACENTER_ID + 1);
                }
            }
        } catch (Exception e) {
            // 如果获取失败，使用随机数
            id = new SecureRandom().nextInt((int) (MAX_DATACENTER_ID + 1));
        }
        return id;
    }
    
    /**
     * 获取机器ID
     */
    private long getWorkerId(long datacenterId) {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null) {
            return Math.abs(hostname.hashCode()) % (MAX_WORKER_ID + 1);
        }
        // 如果获取失败，使用随机数
        return new SecureRandom().nextInt((int) (MAX_WORKER_ID + 1));
    }
}