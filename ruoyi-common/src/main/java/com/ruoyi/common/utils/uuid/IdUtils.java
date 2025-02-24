package com.ruoyi.common.utils.uuid;

/**
 * ID生成器工具类
 * 
 * @author ruoyi
 */
public class IdUtils {
    private static final long WORKER_ID = 1; // 工作节点ID
    private static final long DATACENTER_ID = 1; // 数据中心ID
    private static final long SEQUENCE_MASK = ~(-1L << 12); // 序列号的掩码
    private static final long EPOCH = 1288834974657L; // 起始时间戳
    private static long sequence = 0L; // 序列号
    private static long lastTimestamp = -1L; // 上一时间戳

    /**
     * 获取随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 随机UUID
     */
    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * 获取雪花算法生成的唯一ID
     *
     * @return 雪花算法生成的唯一ID
     */
    public static synchronized Long snowflakeId() {
        long timestamp = System.currentTimeMillis();

        // 如果时间回退，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("生成id失败");
        }

        // 如果当前时间戳等于上一次时间戳，则增加序列号
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK; // 处理同一毫秒内的序列号
            if (sequence == 0) { // 如果序列号用尽，等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L; // 如果时间戳变化，重置序列号
        }

        lastTimestamp = timestamp;

        // 生成ID
        return ((timestamp - EPOCH) << 22) | (DATACENTER_ID << 17) | (WORKER_ID << 12) | sequence;
    }

    // 等待下一毫秒，避免时间回退问题
    private static Long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}

