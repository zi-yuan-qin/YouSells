package com.yousells.modules.report.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.report.convert.ReportConvert;
import com.yousells.modules.report.convert.ReportConvert.DailyAggregation;
import com.yousells.modules.report.convert.ReportConvert.WeekRange;
import com.yousells.modules.report.convert.ReportConvert.WeeklyAggregation;
import com.yousells.modules.report.dto.DailyReportCreateRequest;
import com.yousells.modules.report.dto.WeeklyReportCreateRequest;
import com.yousells.modules.report.entity.DailyReportEntity;
import com.yousells.modules.report.entity.WeeklyReportEntity;
import com.yousells.modules.report.mapper.DailyReportMapper;
import com.yousells.modules.report.mapper.ReportAggregationMapper;
import com.yousells.modules.report.mapper.WeeklyReportMapper;
import com.yousells.modules.report.service.ReportService;
import com.yousells.modules.report.vo.DailyReportVo;
import com.yousells.modules.report.vo.WeeklyReportVo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final DailyReportMapper dailyReportMapper;
    private final WeeklyReportMapper weeklyReportMapper;
    private final ReportAggregationMapper aggregationMapper;
    private final UserMapper userMapper;

    public ReportServiceImpl(DailyReportMapper dailyReportMapper,
                              WeeklyReportMapper weeklyReportMapper,
                              ReportAggregationMapper aggregationMapper,
                              UserMapper userMapper) {
        this.dailyReportMapper = dailyReportMapper;
        this.weeklyReportMapper = weeklyReportMapper;
        this.aggregationMapper = aggregationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public DailyReportVo getDailyReport(LocalDate date) {
        LoginUser user = SecurityUserContext.requireCurrentUser();
        DailyReportVo vo = dailyReportMapper.selectByDateAndUser(date, user.userId());
        if (vo == null) {
            DailyAggregation agg = aggregateDaily(date, user.userId());
            return DailyReportVo.preview(date, user.userId(), user.realName(), agg);
        }
        return vo;
    }

    @Override
    public PageResponse<DailyReportVo> pageDailyReports(int page, int pageSize) {
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 20 : pageSize;
        List<Long> visibleUserIds = resolveVisibleUserIds();
        IPage<DailyReportVo> result = dailyReportMapper.selectPageWithUser(
                Page.of(safePage, safePageSize), visibleUserIds);
        return PageResponse.of(result.getRecords(), safePage, safePageSize, result.getTotal());
    }

    @Override
    public Long createDailyReport(DailyReportCreateRequest request) {
        LoginUser user = SecurityUserContext.requireCurrentUser();
        DailyAggregation agg = aggregateDaily(request.reportDate(), user.userId());
        DailyReportEntity entity = ReportConvert.toDailyEntity(request, user.userId(), agg);
        try {
            dailyReportMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCodeConstants.STATUS_CONFLICT,
                    "该日期已有日报记录，请勿重复提交");
        }
        return entity.getId();
    }

    @Override
    public void updateDailyReport(Long id, DailyReportCreateRequest request) {
        DailyReportEntity entity = dailyReportMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "daily report not found");
        }
        if (!entity.getReportDate().equals(LocalDate.now())) {
            throw new BusinessException(ErrorCodeConstants.STATUS_CONFLICT, "只能修改当天的日报");
        }
        ReportConvert.applyDailyUpdate(entity, request);
        dailyReportMapper.updateById(entity);
    }

    @Override
    public WeeklyReportVo getWeeklyReport(String weekKey) {
        LoginUser user = SecurityUserContext.requireCurrentUser();
        WeeklyReportVo vo = weeklyReportMapper.selectByWeekAndUser(weekKey, user.userId());
        if (vo == null) {
            WeekRange range = ReportConvert.resolveWeekRange(weekKey);
            WeeklyAggregation agg = aggregateWeekly(range, user.userId());
            return WeeklyReportVo.preview(weekKey, user.userId(), user.realName(), agg);
        }
        return vo;
    }

    @Override
    public PageResponse<WeeklyReportVo> pageWeeklyReports(int page, int pageSize) {
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 20 : pageSize;
        List<Long> visibleUserIds = resolveVisibleUserIds();
        IPage<WeeklyReportVo> result = weeklyReportMapper.selectPageWithUser(
                Page.of(safePage, safePageSize), visibleUserIds);
        return PageResponse.of(result.getRecords(), safePage, safePageSize, result.getTotal());
    }

    @Override
    public Long createWeeklyReport(WeeklyReportCreateRequest request) {
        LoginUser user = SecurityUserContext.requireCurrentUser();
        WeekRange range = ReportConvert.resolveWeekRange(request.weekKey());
        WeeklyAggregation agg = aggregateWeekly(range, user.userId());
        WeeklyReportEntity entity = ReportConvert.toWeeklyEntity(request, user.userId(), agg);
        try {
            weeklyReportMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCodeConstants.STATUS_CONFLICT,
                    "该周已有周报记录，请勿重复提交");
        }
        return entity.getId();
    }

    @Override
    public void updateWeeklyReport(Long id, WeeklyReportCreateRequest request) {
        WeeklyReportEntity entity = weeklyReportMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "weekly report not found");
        }
        WeekRange range = ReportConvert.resolveWeekRange(request.weekKey());
        LocalDate today = LocalDate.now();
        if (today.isBefore(range.weekStart()) || today.isAfter(range.weekEnd())) {
            throw new BusinessException(ErrorCodeConstants.STATUS_CONFLICT, "只能修改本周的周报");
        }
        ReportConvert.applyWeeklyUpdate(entity, request);
        weeklyReportMapper.updateById(entity);
    }

    private DailyAggregation aggregateDaily(LocalDate date, Long userId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        int newCustomerCount = aggregationMapper.countNewCustomers(start, end, userId);
        int followUpCount = aggregationMapper.countFollowUps(start, end, userId);
        int taskCompletedCount = aggregationMapper.countCompletedTasks(start, end, userId);

        return new DailyAggregation(newCustomerCount, followUpCount, 0,
                taskCompletedCount, null, null);
    }

    private WeeklyAggregation aggregateWeekly(WeekRange range, Long userId) {
        LocalDateTime start = range.weekStart().atStartOfDay();
        LocalDateTime end = range.weekEnd().plusDays(1).atStartOfDay();

        int newCustomerCount = aggregationMapper.countNewCustomers(start, end, userId);
        int followUpCount = aggregationMapper.countFollowUps(start, end, userId);
        int convertedCount = aggregationMapper.countConvertedCustomers(start, end, userId);
        int taskCompletedCount = aggregationMapper.countCompletedTasks(start, end, userId);

        return new WeeklyAggregation(newCustomerCount, followUpCount, 0,
                convertedCount, taskCompletedCount, null, null, null,
                range.weekStart(), range.weekEnd());
    }

    private List<Long> resolveVisibleUserIds() {
        LoginUser user = SecurityUserContext.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        ids.add(user.userId());
        ids.addAll(DataScopeHelper.getSubordinateIds(user.userId(), userMapper));
        return ids;
    }
}
