package com.yousells.modules.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.report.dto.DailyReportCreateRequest;
import com.yousells.modules.report.dto.WeeklyReportCreateRequest;
import com.yousells.modules.report.entity.DailyReportEntity;
import com.yousells.modules.report.entity.WeeklyReportEntity;
import com.yousells.modules.report.mapper.DailyReportMapper;
import com.yousells.modules.report.mapper.ReportAggregationMapper;
import com.yousells.modules.report.mapper.WeeklyReportMapper;
import com.yousells.modules.report.service.impl.ReportServiceImpl;
import com.yousells.modules.report.vo.DailyReportVo;
import com.yousells.modules.report.vo.WeeklyReportVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    @Mock
    private DailyReportMapper dailyReportMapper;

    @Mock
    private WeeklyReportMapper weeklyReportMapper;

    @Mock
    private ReportAggregationMapper aggregationMapper;

    @Mock
    private UserMapper userMapper;

    private ReportServiceImpl service;
    private MockedStatic<SecurityUserContext> securityContextMock;

    private static final LoginUser TEST_USER = new LoginUser(1L, "admin", "秦梓源", "T2", null);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportServiceImpl(dailyReportMapper, weeklyReportMapper, aggregationMapper, userMapper);
        securityContextMock = Mockito.mockStatic(SecurityUserContext.class);
        securityContextMock.when(SecurityUserContext::getCurrentUser).thenReturn(TEST_USER);
        securityContextMock.when(SecurityUserContext::requireCurrentUser).thenReturn(TEST_USER);
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();
    }

    @Test
    void shouldGetDailyReportWithAggregation() {
        when(dailyReportMapper.selectByDateAndUser(any(), anyLong())).thenReturn(null);
        when(aggregationMapper.countNewCustomers(any(), any(), anyLong())).thenReturn(3);
        when(aggregationMapper.countFollowUps(any(), any(), anyLong())).thenReturn(5);
        when(aggregationMapper.countCompletedTasks(any(), any(), anyLong())).thenReturn(2);

        DailyReportVo result = service.getDailyReport(LocalDate.of(2026, 5, 21));

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 5, 21), result.reportDate());
        assertEquals(3, result.newCustomerCount());
        assertEquals(5, result.followUpCount());
        assertEquals(2, result.taskCompletedCount());
        assertNull(result.summary());
    }

    @Test
    void shouldReturnExistingDailyReport() {
        DailyReportVo existing = new DailyReportVo(1L, LocalDate.of(2026, 5, 21), 1L, "秦梓源",
                3, 5, 1, 2, null, null, "今天完成了很多工作", "没问题", "明天继续");
        when(dailyReportMapper.selectByDateAndUser(any(), anyLong())).thenReturn(existing);

        DailyReportVo result = service.getDailyReport(LocalDate.of(2026, 5, 21));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("今天完成了很多工作", result.summary());
    }

    @Test
    void shouldCreateDailyReport() {
        when(aggregationMapper.countNewCustomers(any(), any(), anyLong())).thenReturn(2);
        when(aggregationMapper.countFollowUps(any(), any(), anyLong())).thenReturn(3);
        when(aggregationMapper.countCompletedTasks(any(), any(), anyLong())).thenReturn(1);
        when(dailyReportMapper.insert(Mockito.<DailyReportEntity>any())).thenAnswer(inv -> {
            DailyReportEntity e = inv.getArgument(0);
            e.setId(100L);
            return 1;
        });

        DailyReportCreateRequest request = new DailyReportCreateRequest(
                LocalDate.of(2026, 5, 21), "完成公共安排模块开发", "无问题", "开始日报周报模块");
        Long id = service.createDailyReport(request);

        assertEquals(100L, id);
        verify(dailyReportMapper).insert(Mockito.<DailyReportEntity>argThat(e ->
                "完成公共安排模块开发".equals(e.getSummary())
                        && e.getNewCustomerCount() == 2
                        && e.getFollowUpCount() == 3
        ));
    }

    @Test
    void shouldRejectDuplicateDailyReport() {
        when(aggregationMapper.countNewCustomers(any(), any(), anyLong())).thenReturn(0);
        when(aggregationMapper.countFollowUps(any(), any(), anyLong())).thenReturn(0);
        when(aggregationMapper.countCompletedTasks(any(), any(), anyLong())).thenReturn(0);
        when(dailyReportMapper.insert(Mockito.<DailyReportEntity>any()))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("dup"));

        DailyReportCreateRequest request = new DailyReportCreateRequest(
                LocalDate.of(2026, 5, 21), "test", null, "test");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createDailyReport(request));
        assertEquals(ErrorCodeConstants.STATUS_CONFLICT, ex.getCode());
        assertTrue(ex.getMessage().contains("重复提交"));
    }

    @Test
    void shouldUpdateDailyReportSameDay() {
        DailyReportEntity entity = new DailyReportEntity();
        entity.setId(1L);
        entity.setReportDate(LocalDate.now());
        entity.setSummary("旧内容");
        when(dailyReportMapper.selectById(1L)).thenReturn(entity);
        when(dailyReportMapper.updateById(Mockito.<DailyReportEntity>any())).thenReturn(1);

        DailyReportCreateRequest request = new DailyReportCreateRequest(
                LocalDate.now(), "更新后的内容", "新问题", "新计划");
        service.updateDailyReport(1L, request);

        verify(dailyReportMapper).updateById(Mockito.<DailyReportEntity>argThat(e ->
                "更新后的内容".equals(e.getSummary())));
    }

    @Test
    void shouldRejectUpdateNextDay() {
        DailyReportEntity entity = new DailyReportEntity();
        entity.setId(1L);
        entity.setReportDate(LocalDate.now().minusDays(1));
        when(dailyReportMapper.selectById(1L)).thenReturn(entity);

        DailyReportCreateRequest request = new DailyReportCreateRequest(
                LocalDate.now().minusDays(1), "更新", null, "计划");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateDailyReport(1L, request));
        assertEquals(ErrorCodeConstants.STATUS_CONFLICT, ex.getCode());
    }

    @Test
    void shouldGetWeeklyReportWithAggregation() {
        when(weeklyReportMapper.selectByWeekAndUser(any(), anyLong())).thenReturn(null);
        when(aggregationMapper.countNewCustomers(any(), any(), anyLong())).thenReturn(10);
        when(aggregationMapper.countFollowUps(any(), any(), anyLong())).thenReturn(20);
        when(aggregationMapper.countConvertedCustomers(any(), any(), anyLong())).thenReturn(1);
        when(aggregationMapper.countCompletedTasks(any(), any(), anyLong())).thenReturn(5);

        WeeklyReportVo result = service.getWeeklyReport("2026-W21");

        assertNotNull(result);
        assertEquals("2026-W21", result.weekKey());
        assertEquals(10, result.newCustomerCount());
        assertEquals(20, result.followUpCount());
        assertEquals(1, result.convertedCount());
        assertEquals(5, result.taskCompletedCount());
        assertNull(result.summary());
    }

    @Test
    void shouldCreateWeeklyReport() {
        when(aggregationMapper.countNewCustomers(any(), any(), anyLong())).thenReturn(8);
        when(aggregationMapper.countFollowUps(any(), any(), anyLong())).thenReturn(15);
        when(aggregationMapper.countConvertedCustomers(any(), any(), anyLong())).thenReturn(2);
        when(aggregationMapper.countCompletedTasks(any(), any(), anyLong())).thenReturn(3);
        when(weeklyReportMapper.insert(Mockito.<WeeklyReportEntity>any())).thenAnswer(inv -> {
            WeeklyReportEntity e = inv.getArgument(0);
            e.setId(200L);
            return 1;
        });

        WeeklyReportCreateRequest request = new WeeklyReportCreateRequest(
                "2026-W21", "本周推进顺利", "无大问题", "下周继续推进客户");
        Long id = service.createWeeklyReport(request);

        assertEquals(200L, id);
        verify(weeklyReportMapper).insert(Mockito.<WeeklyReportEntity>argThat(e ->
                "本周推进顺利".equals(e.getSummary())
                        && e.getNewCustomerCount() == 8
                        && e.getConvertedCount() == 2
        ));
    }

    @Test
    void shouldRejectDuplicateWeeklyReport() {
        when(aggregationMapper.countNewCustomers(any(), any(), anyLong())).thenReturn(0);
        when(aggregationMapper.countFollowUps(any(), any(), anyLong())).thenReturn(0);
        when(aggregationMapper.countConvertedCustomers(any(), any(), anyLong())).thenReturn(0);
        when(aggregationMapper.countCompletedTasks(any(), any(), anyLong())).thenReturn(0);
        when(weeklyReportMapper.insert(Mockito.<WeeklyReportEntity>any()))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("dup"));

        WeeklyReportCreateRequest request = new WeeklyReportCreateRequest(
                "2026-W21", "test", null, "test");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createWeeklyReport(request));
        assertEquals(ErrorCodeConstants.STATUS_CONFLICT, ex.getCode());
    }

    @Test
    void shouldThrowNotFoundWhenUpdateMissingDailyReport() {
        when(dailyReportMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateDailyReport(999L, new DailyReportCreateRequest(
                        LocalDate.now(), "test", null, "test")));
        assertEquals(ErrorCodeConstants.NOT_FOUND, ex.getCode());
    }

    @Test
    void shouldPageDailyHistory() {
        List<DailyReportVo> records = List.of(
                new DailyReportVo(1L, LocalDate.of(2026, 5, 21), 1L, "秦梓源",
                        3, 5, 1, 2, null, null, "工作内容", "无", "计划"),
                new DailyReportVo(2L, LocalDate.of(2026, 5, 20), 1L, "秦梓源",
                        2, 3, 0, 1, null, null, "昨天工作", "无", "计划")
        );
        Page<DailyReportVo> pageResult = new Page<>(1, 20, 2);
        pageResult.setRecords(records);
        when(dailyReportMapper.selectPageWithUser(any(), any())).thenReturn(pageResult);

        PageResponse<DailyReportVo> result = service.pageDailyReports(1, 20);

        assertEquals(2, result.total());
        assertEquals(2, result.list().size());
    }
}
