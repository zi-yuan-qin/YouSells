package com.yousells.modules.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_tags")
public class CustomerTagEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    private String tagType;

    private String tagColor;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;
}
