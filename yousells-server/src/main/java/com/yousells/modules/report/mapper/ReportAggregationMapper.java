package com.yousells.modules.report.mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

public interface ReportAggregationMapper {
    int countNewCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);
    int countFollowUps(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);
    int countCompletedTasks(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);
    int countConvertedCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);
}
