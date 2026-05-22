package com.yousells.modules.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.report.entity.WeeklyReportEntity;
import com.yousells.modules.report.vo.WeeklyReportVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WeeklyReportMapper extends BaseMapper<WeeklyReportEntity> {

    IPage<WeeklyReportVo> selectPageWithUser(Page<?> page,
                                              @Param("visibleUserIds") List<Long> visibleUserIds);

    WeeklyReportVo selectByWeekAndUser(@Param("weekKey") String weekKey,
                                        @Param("userId") Long userId);

    WeeklyReportVo selectByIdWithUser(@Param("id") Long id);
}
