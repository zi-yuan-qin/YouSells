package com.yousells.modules.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.task.entity.TaskBoardEntity;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import com.yousells.modules.task.vo.TaskDetailVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskBoardMapper extends BaseMapper<TaskBoardEntity> {

    IPage<TaskBoardItemVo> selectPageWithUsers(Page<?> page,
                                                @Param("status") String status,
                                                @Param("ownerUserId") Long ownerUserId,
                                                @Param("direction") String direction,
                                                @Param("visibleOwnerIds") List<Long> visibleOwnerIds);

    List<TaskBoardItemVo> selectAllWithUsers(@Param("visibleOwnerIds") List<Long> visibleOwnerIds);

    TaskDetailVo selectDetailById(@Param("id") Long id);
}
