package com.yousells.modules.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yousells.modules.notification.entity.NotificationEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity> {

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int countUnread(@Param("userId") Long userId);

    @Update("UPDATE notifications SET is_read = 1, updated_at = NOW() WHERE user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int markAllRead(@Param("userId") Long userId);

    @Delete("DELETE FROM notifications WHERE id = #{id} AND user_id = #{userId}")
    int permanentDelete(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notifications SET is_deleted = 1, updated_at = NOW() WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notifications SET is_deleted = 0, updated_at = NOW() WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 1")
    int restore(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND is_deleted = 1 ORDER BY updated_at DESC LIMIT #{offset}, #{limit}")
    List<NotificationEntity> pageTrash(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_deleted = 1")
    int countTrash(@Param("userId") Long userId);

    @Delete("DELETE FROM notifications WHERE user_id = #{userId} AND is_deleted = 1")
    int permanentDeleteAll(@Param("userId") Long userId);
}
