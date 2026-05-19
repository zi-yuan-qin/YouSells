package com.yousells.modules.followup.convert;

import com.yousells.modules.followup.dto.FollowUpCreateRequest;
import com.yousells.modules.followup.entity.FollowUpEntity;
import com.yousells.modules.followup.vo.FollowUpVo;

public class FollowUpConvert {

    public static FollowUpVo toVo(FollowUpEntity entity, String operatorDisplayName, String ownerDisplayName) {
        return new FollowUpVo(
                entity.getId(),
                entity.getCustomerId(),
                entity.getFollowType(),
                entity.getCommunicatedContent(),
                entity.getCustomerFeedback(),
                entity.getCurrentConcern(),
                entity.getNextAction(),
                entity.getNextFollowAt(),
                operatorDisplayName,
                ownerDisplayName,
                entity.getCreatedAt()
        );
    }

    public static FollowUpEntity toEntity(FollowUpCreateRequest request) {
        FollowUpEntity entity = new FollowUpEntity();
        entity.setCustomerId(request.customerId());
        entity.setFollowType(request.followType());
        entity.setCommunicatedContent(request.communicatedContent());
        entity.setCustomerFeedback(request.customerFeedback());
        entity.setCurrentConcern(request.currentConcern());
        entity.setNextAction(request.nextAction());
        entity.setNextFollowAt(request.nextFollowAt());
        return entity;
    }
}
