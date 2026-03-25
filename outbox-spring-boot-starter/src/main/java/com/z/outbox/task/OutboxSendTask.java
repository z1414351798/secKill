package com.z.outbox.task;

import com.z.outbox.domain.OutboxEvent;
import com.z.outbox.mapper.OutboxEventMapper;
import com.z.outbox.properties.OutboxProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
public class OutboxSendTask {

    private final OutboxEventMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties properties;

    private ExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(OutboxSendTask.class);

    public OutboxSendTask(OutboxEventMapper mapper,
                          KafkaTemplate<String, String> kafkaTemplate,
                          OutboxProperties properties) {
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    /**
     * ✅ 正确初始化线程池（保证拿到配置值）
     */
    @PostConstruct
    public void init() {
        this.executor = new ThreadPoolExecutor(
                properties.getThreadPoolSize(),
                properties.getThreadPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), // ✅ 有界队列（防OOM）
                new ThreadPoolExecutor.CallerRunsPolicy() // ✅ 背压策略
        );

        log.info("Outbox线程池初始化完成，线程数={}", properties.getThreadPoolSize());
    }

    /**
     * ✅ 优雅关闭
     */
    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * ✅ 定时扫描
     */
    @Scheduled(fixedDelayString = "${outbox.scan-interval:1000}")
    public void send() {
        try {
            List<OutboxEvent> list =
                    mapper.selectReadyEvents(properties.getBatchSize(), properties.getTopics());

            if (list == null || list.isEmpty()) {
                return;
            }

            for (OutboxEvent event : list) {
                executor.submit(() -> processEvent(event));
            }

        } catch (Exception e) {
            log.error("Outbox扫描异常", e);
        }
    }

    /**
     * ✅ 处理单条消息
     */
    private void processEvent(OutboxEvent event) {

        try {
            // 1️⃣ CAS 抢占执行权（防重复）
            if (mapper.markSending(event.getId()) == 0) {
                return;
            }

            // 2️⃣ 发送 Kafka（异步）
            kafkaTemplate.send(event.getTopic(), event.getBizId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Kafka发送失败, eventId={}", event.getId(), ex);
                            handleSendFail(event);
                        } else {
                            mapper.markSent(event.getId());
                        }
                    });

        } catch (Exception e) {
            log.error("处理OutboxEvent异常，eventId={}", event.getId(), e);
            handleSendFail(event);
        }
    }

    /**
     * ✅ 失败处理（带退避思想）
     */
    private void handleSendFail(OutboxEvent event) {
        try {
            if (event.getRetryCount() >= event.getMaxRetry()) {
                mapper.markFail(event.getId());
            } else {
                LocalDateTime nextRetryTime = calculateNextRetryTime(event.getRetryCount());
                mapper.retryWithBackoff(event.getId(), nextRetryTime);
            }
        } catch (Exception e) {
            log.error("Outbox失败处理异常, eventId={}", event.getId(), e);
        }
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        long delayMillis = calculateExponentialBackoff(retryCount);
        return LocalDateTime.now().plusNanos(delayMillis * 1_000_000);
    }

    private long calculateExponentialBackoff(int retryCount) {
        long base = 1000L; // 1秒
        long max = 60_000L; // 最大1分钟

        long delay = base * (1L << retryCount);

        return Math.min(delay, max);
    }
}