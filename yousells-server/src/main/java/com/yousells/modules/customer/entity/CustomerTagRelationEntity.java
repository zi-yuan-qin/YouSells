package com.yousells.modules.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_tag_relations")
public class CustomerTagRelationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private Long tagId;

    private LocalDateTime createdAt;
}
