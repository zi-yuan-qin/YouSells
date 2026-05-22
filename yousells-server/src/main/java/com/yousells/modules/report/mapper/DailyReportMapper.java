package com.yousells.modules.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.report.entity.DailyReportEntity;
import com.yousells.modules.report.vo.DailyReportVo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyReportMapper extends BaseMapper<DailyReportEntity> {

    IPage<DailyReportVo> selectPageWithUser(Page<?> page,
                                             @Param("visibleUserIds") List<Long> visibleUserIds);

    DailyReportVo selectByDateAndUser(@Param("date") LocalDate date,
                                       @Param("userId") Long userId);

    DailyReportVo selectByIdWithUser(@Param("id") Long id);
}
