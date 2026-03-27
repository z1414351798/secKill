package com.z.outbox.mapper;

import com.z.outbox.domain.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OutboxEventMapper {

    int insert(OutboxEvent event);

    List<OutboxEvent> selectReadyEvents(@Param("limit") int limit, @Param("topics") List<String> topics);

    int markSending(@Param("id") Long id);

    int markSent(@Param("id") Long id);

    int resetToInit(@Param("id") Long id);

    int markFail(@Param("id") Long id);

    boolean retryWithBackoff(@Param("id") Long id, @Param("nextRetryTime")LocalDateTime nextRetryTime);
}