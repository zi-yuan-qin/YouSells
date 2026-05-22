package com.yousells.modules.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.task.dto.TaskCreateRequest;
import com.yousells.modules.task.dto.TaskLogCreateRequest;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.dto.TaskStatusUpdateRequest;
import com.yousells.modules.task.entity.TaskBoardEntity;
import com.yousells.modules.task.entity.TaskLogEntity;
import com.yousells.modules.task.mapper.TaskBoardMapper;
import com.yousells.modules.task.mapper.TaskLogMapper;
import com.yousells.modules.task.service.impl.TaskBoardServiceImpl;
import com.yousells.modules.task.vo.TaskBoardColumnVo;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import com.yousells.modules.task.vo.TaskDetailVo;
import com.yousells.modules.task.vo.TaskDetailWithLogsVo;
import com.yousells.modules.task.vo.TaskLogVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskBoardServiceTest {

    @Mock
    private TaskBoardMapper taskBoardMapper;

    @Mock
    private TaskLogMapper taskLogMapper;

    @Mock
    private UserMapper userMapper;

    private TaskBoardServiceImpl service;
    private MockedStatic<SecurityUserContext> securityContextMock;

    private static final LoginUser TEST_USER = new LoginUser(1L, "admin", "秦梓源", "T2", null);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TaskBoardServiceImpl(taskBoardMapper, taskLogMapper, userMapper);
        securityContextMock = Mockito.mockStatic(SecurityUserContext.class);
        securityContextMock.when(SecurityUserContext::getCurrentUser).thenReturn(TEST_USER);
        securityContextMock.when(SecurityUserContext::requireCurrentUser).thenReturn(TEST_USER);
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();
    }

    @Test
    void shouldPageTasks() {
        List<TaskBoardItemVo> records = List.of(
                new TaskBoardItemVo(1L, "任务1", "向下派发", "待开始", "高", "秦梓源", null, null, null),
                new TaskBoardItemVo(2L, "任务2", "自己规划", "进行中", "中", "秦梓源", null, null, null)
        );
        Page<TaskBoardItemVo> pageResult = new Page<>(1, 20, 2);
        pageResult.setRecords(records);
        when(taskBoardMapper.selectPageWithUsers(any(), any(), any(), any(), any())).thenReturn(pageResult);

        PageResponse<TaskBoardItemVo> result = service.pageTasks(new TaskQueryRequest(1, 20, null, null, null));

        assertEquals(2, result.total());
        assertEquals(2, result.list().size());
    }

    @Test
    void shouldPageTasksWithFilters() {
        List<TaskBoardItemVo> records = List.of(
                new TaskBoardItemVo(1L, "任务1", "向下派发", "待开始", "高", "秦梓源", null, null, null)
        );
        Page<TaskBoardItemVo> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(records);
        when(taskBoardMapper.selectPageWithUsers(any(), any(), any(), any(), any())).thenReturn(pageResult);

        PageResponse<TaskBoardItemVo> result = service.pageTasks(
                new TaskQueryRequest(1, 20, "待开始", null, "向下派发"));

        assertEquals(1, result.total());
        assertTrue(result.list().stream().allMatch(t -> "待开始".equals(t.status())));
    }

    @Test
    void shouldReturnEmptyPage() {
        Page<TaskBoardItemVo> pageResult = new Page<>(1, 20, 0);
        pageResult.setRecords(List.of());
        when(taskBoardMapper.selectPageWithUsers(any(), any(), any(), any(), any())).thenReturn(pageResult);

        PageResponse<TaskBoardItemVo> result = service.pageTasks(new TaskQueryRequest(1, 20, null, null, null));

        assertEquals(0, result.total());
        assertTrue(result.list().isEmpty());
    }

    @Test
    void shouldUseDefaultPageParams() {
        Page<TaskBoardItemVo> pageResult = new Page<>(1, 20, 0);
        pageResult.setRecords(List.of());
        when(taskBoardMapper.selectPageWithUsers(any(), any(), any(), any(), any())).thenReturn(pageResult);

        service.pageTasks(new TaskQueryRequest(0, -1, null, null, null));

        verify(taskBoardMapper).selectPageWithUsers(
                Mockito.<Page<?>>argThat(p -> p.getCurrent() == 1 && p.getSize() == 20),
                any(), any(), any(), any());
    }

    @Test
    void shouldListBoard() {
        List<TaskBoardItemVo> tasks = List.of(
                new TaskBoardItemVo(1L, "任务1", "向下派发", "待开始", "高", "秦梓源", null, null, null),
                new TaskBoardItemVo(2L, "任务2", "自己规划", "进行中", "中", "秦梓源", null, null, null),
                new TaskBoardItemVo(3L, "任务3", "向上建议", "已完成", "低", "秦梓源", null, null, null)
        );
        when(taskBoardMapper.selectAllWithUsers(any())).thenReturn(tasks);

        List<TaskBoardColumnVo> board = service.listBoard();

        assertEquals(3, board.size());
        assertEquals("待开始", board.get(0).status());
        assertEquals(1, board.get(0).items().size());
        assertEquals("进行中", board.get(1).status());
        assertEquals(1, board.get(1).items().size());
        assertEquals("已完成", board.get(2).status());
        assertEquals(1, board.get(2).items().size());
    }

    @Test
    void shouldListBoardEmpty() {
        when(taskBoardMapper.selectAllWithUsers(any())).thenReturn(List.of());

        List<TaskBoardColumnVo> board = service.listBoard();

        assertEquals(3, board.size());
        assertTrue(board.stream().allMatch(col -> col.items().isEmpty()));
    }

    @Test
    void shouldCreateDownwardTask() {
        when(taskBoardMapper.insert(Mockito.<TaskBoardEntity>any())).thenAnswer(inv -> {
            TaskBoardEntity e = inv.getArgument(0);
            e.setId(100L);
            return 1;
        });

        Long id = service.createTask(new TaskCreateRequest(
                "向下派发任务", "任务描述", "向下派发", 2L, null, "高", null));

        assertEquals(100L, id);
        verify(taskBoardMapper).insert(Mockito.<TaskBoardEntity>argThat(e ->
                "向下派发任务".equals(e.getTaskTitle())
                        && "向下派发".equals(e.getDirection())
                        && e.getOwnerUserId().equals(2L)
                        && "待开始".equals(e.getStatus())
        ));
    }

    @Test
    void shouldCreateSelfTask() {
        when(taskBoardMapper.insert(Mockito.<TaskBoardEntity>any())).thenAnswer(inv -> {
            TaskBoardEntity e = inv.getArgument(0);
            e.setId(101L);
            return 1;
        });

        Long id = service.createTask(new TaskCreateRequest(
                "自己规划任务", null, "自己规划", 1L, null, null, null));

        assertEquals(101L, id);
        verify(taskBoardMapper).insert(Mockito.<TaskBoardEntity>argThat(e ->
                "自己规划任务".equals(e.getTaskTitle())
                        && "自己规划".equals(e.getDirection())
                        && "中".equals(e.getPriority())
        ));
    }

    @Test
    void shouldCreateUpwardTask() {
        when(taskBoardMapper.insert(Mockito.<TaskBoardEntity>any())).thenAnswer(inv -> {
            TaskBoardEntity e = inv.getArgument(0);
            e.setId(102L);
            return 1;
        });

        Long id = service.createTask(new TaskCreateRequest(
                "向上建议任务", "建议内容", "向上建议", 1L, 3L, "低", null));

        assertEquals(102L, id);
        verify(taskBoardMapper).insert(Mockito.<TaskBoardEntity>argThat(e ->
                "向上建议".equals(e.getDirection())
                        && e.getSuggestedToUserId().equals(3L)
        ));
    }

    @Test
    void shouldUpdateTaskStatusWithAutoLog() {
        TaskBoardEntity existing = new TaskBoardEntity();
        existing.setId(1L);
        existing.setStatus("待开始");
        when(taskBoardMapper.selectById(1L)).thenReturn(existing);
        when(taskBoardMapper.updateById(Mockito.<TaskBoardEntity>any())).thenReturn(1);
        when(taskLogMapper.insert(Mockito.<TaskLogEntity>any())).thenReturn(1);

        service.updateTaskStatus(1L, new TaskStatusUpdateRequest("进行中"));

        assertEquals("进行中", existing.getStatus());
        verify(taskBoardMapper).updateById(Mockito.<TaskBoardEntity>any());
        verify(taskLogMapper).insert(Mockito.<TaskLogEntity>argThat(log ->
                "状态变更".equals(log.getType())
                        && "待开始".equals(log.getFromStatus())
                        && "进行中".equals(log.getToStatus())
        ));
    }

    @Test
    void shouldAddManualLog() {
        TaskBoardEntity existing = new TaskBoardEntity();
        existing.setId(1L);
        when(taskBoardMapper.selectById(1L)).thenReturn(existing);
        when(taskLogMapper.insert(Mockito.<TaskLogEntity>any())).thenReturn(1);

        service.addTaskLog(1L, new TaskLogCreateRequest("备注", "添加了一条备注"));

        verify(taskLogMapper).insert(Mockito.<TaskLogEntity>argThat(log ->
                "备注".equals(log.getType())
                        && "添加了一条备注".equals(log.getContent())
                        && log.getUserId().equals(1L)
        ));
    }

    @Test
    void shouldGetTaskDetailWithLogs() {
        TaskDetailVo detail = new TaskDetailVo(1L, "任务标题", "任务描述", "自己规划", "待开始", "高",
                "秦梓源", "秦梓源", null, null, 1L, 1L, null);
        when(taskBoardMapper.selectDetailById(1L)).thenReturn(detail);
        when(taskLogMapper.selectByTaskId(1L)).thenReturn(List.of(
                new TaskLogVo(1L, 1L, 1L, "秦梓源", "状态变更", null, "待开始", "进行中", LocalDateTime.now())
        ));

        TaskDetailWithLogsVo result = service.getTask(1L);

        assertNotNull(result);
        assertEquals("任务标题", result.task().taskTitle());
        assertEquals(1, result.logs().size());
    }

    @Test
    void shouldThrowNotFoundForMissingTask() {
        when(taskBoardMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateTaskStatus(999L, new TaskStatusUpdateRequest("进行中")));
        assertEquals(ErrorCodeConstants.NOT_FOUND, ex.getCode());
    }

    @Test
    void shouldThrowNotFoundForMissingTaskDetail() {
        when(taskBoardMapper.selectDetailById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getTask(999L));
        assertEquals(ErrorCodeConstants.NOT_FOUND, ex.getCode());
    }
}
