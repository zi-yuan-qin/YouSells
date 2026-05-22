package com.yousells.modules.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yousells.modules.task.entity.TaskLogEntity;
import com.yousells.modules.task.vo.TaskLogVo;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface TaskLogMapper extends BaseMapper<TaskLogEntity> {
    List<TaskLogVo> selectByTaskId(@Param("taskId") Long taskId);
}
