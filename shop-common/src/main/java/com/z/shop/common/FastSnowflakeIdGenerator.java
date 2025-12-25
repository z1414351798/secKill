package com.z.shop.common;

import java.util.concurrent.atomic.AtomicLong;

public class FastSnowflakeIdGenerator {

    // 起始时间戳（2024-01-01）
    private static final long START_TIMESTAMP = 1704067200000L;

    private static final long SEQUENCE_BITS = 12;
    private static final long WORKER_BITS = 5;
    private static final long DATACENTER_BITS = 5;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
    private static final long TIMESTAMP_SHIFT =
            SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    private final long workerId;
    private final long datacenterId;

    /**
     * 高位：timestamp
     * 低位：sequence
     */
    private final AtomicLong lastTimestampAndSequence = new AtomicLong(0);

    public FastSnowflakeIdGenerator(long workerId, long datacenterId) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public long nextId() {
        while (true) {
            long now = System.currentTimeMillis();
            long timestamp = now - START_TIMESTAMP;

            long last = lastTimestampAndSequence.get();
            long lastTimestamp = last >>> SEQUENCE_BITS;
            long lastSequence = last & MAX_SEQUENCE;

            long nextSequence;
            long nextTimestamp;

            if (timestamp < lastTimestamp) {
                // 时钟回拨：直接失败（也可改成等待）
                throw new RuntimeException("Clock moved backwards");
            }

            if (timestamp == lastTimestamp) {
                nextSequence = (lastSequence + 1) & MAX_SEQUENCE;
                if (nextSequence == 0) {
                    // sequence 用完，等下一毫秒
                    timestamp = waitNextMillis(lastTimestamp);
                    nextTimestamp = timestamp;
                } else {
                    nextTimestamp = lastTimestamp;
                }
            } else {
                nextSequence = 0;
                nextTimestamp = timestamp;
            }

            long next = (nextTimestamp << SEQUENCE_BITS) | nextSequence;

            // CAS 成功才真正生成 ID
            if (lastTimestampAndSequence.compareAndSet(last, next)) {
                return (nextTimestamp << TIMESTAMP_SHIFT)
                        | (datacenterId << DATACENTER_SHIFT)
                        | (workerId << WORKER_SHIFT)
                        | nextSequence;
            }
        }
    }

    private long waitNextMillis(long lastTimestamp) {
        long ts;
        do {
            ts = System.currentTimeMillis() - START_TIMESTAMP;
        } while (ts <= lastTimestamp);
        return ts;
    }
}
