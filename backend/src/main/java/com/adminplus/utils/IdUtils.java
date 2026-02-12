package com.adminplus.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

public class IdUtils {

    private static final long START_TIMESTAMP = 1735689600000L;

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long workerId;
    private static final long datacenterId;
    private static final AtomicLong sequence = new AtomicLong(0);
    private static long lastTimestamp = -1L;

    static {
        workerId = generateWorkerId();
        datacenterId = generateDatacenterId();
    }

    private static long generateWorkerId() {
        String workerIdStr = System.getProperty("snowflake.worker.id");
        if (workerIdStr != null && !workerIdStr.isEmpty()) {
            try {
                long id = Long.parseLong(workerIdStr);
                if (id >= 0 && id <= MAX_WORKER_ID) {
                    return id;
                }
            } catch (NumberFormatException e) {
            }
        }

        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            long hash = hostName.hashCode();
            return Math.abs(hash) & MAX_WORKER_ID;
        } catch (UnknownHostException e) {
            return 1L;
        }
    }

    private static long generateDatacenterId() {
        String datacenterIdStr = System.getProperty("snowflake.datacenter.id");
        if (datacenterIdStr != null && !datacenterIdStr.isEmpty()) {
            try {
                long id = Long.parseLong(datacenterIdStr);
                if (id >= 0 && id <= MAX_DATACENTER_ID) {
                    return id;
                }
            } catch (NumberFormatException e) {
            }
        }

        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            long hash = hostAddress.hashCode();
            return Math.abs(hash) & MAX_DATACENTER_ID;
        } catch (UnknownHostException e) {
            return 1L;
        }
    }

    public static synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            long currentSequence = sequence.incrementAndGet() & SEQUENCE_MASK;
            if (currentSequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence.set(0);
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();
    }

    public static String nextIdStr() {
        return String.valueOf(nextId());
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }
}
