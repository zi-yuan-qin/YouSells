package com.yousells.modules.customer.convert;

import com.yousells.modules.customer.dto.CustomerCreateRequest;
import com.yousells.modules.customer.dto.CustomerUpdateRequest;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.vo.CustomerDetailVo;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.customer.entity.CustomerTagEntity;
import com.yousells.modules.customer.vo.CustomerTagVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomerConvert {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static CustomerListItemVo toListItemVo(CustomerEntity entity, String ownerDisplayName, List<String> tags) {
        return new CustomerListItemVo(
                entity.getId(),
                entity.getCustomerCode(),
                entity.getNickname(),
                entity.getCustomerType(),
                entity.getSourcePlatform(),
                entity.getIntentLevel(),
                entity.getCurrentStage(),
                ownerDisplayName,
                entity.getLastContactAt(),
                entity.getNextFollowAt(),
                tags
        );
    }

    public static CustomerDetailVo toDetailVo(CustomerEntity entity, String ownerDisplayName,
                                              String assistantDisplayName, List<String> tags) {
        return new CustomerDetailVo(
                entity.getId(),
                entity.getCustomerCode(),
                entity.getCustomerType(),
                entity.getNickname(),
                entity.getContactValue(),
                entity.getSourcePlatform(),
                entity.getExpectedMajor(),
                entity.getBaseLevel(),
                entity.getInterestDirection(),
                entity.getIntentLevel(),
                entity.getCurrentStage(),
                entity.getCurrentConcern(),
                entity.getLatestFeedback(),
                entity.getLastContactAt(),
                entity.getNextFollowAction(),
                entity.getNextFollowAt(),
                ownerDisplayName,
                assistantDisplayName,
                tags,
                entity.getRemarks()
        );
    }

    public static CustomerEntity toEntity(CustomerCreateRequest request) {
        CustomerEntity entity = new CustomerEntity();
        entity.setCustomerCode(generateCustomerCode());
        entity.setCustomerType(request.customerType());
        entity.setNickname(request.nickname());
        entity.setContactValue(request.contactValue());
        entity.setSourcePlatform(request.sourcePlatform());
        entity.setExpectedMajor(request.expectedMajor());
        entity.setBaseLevel(request.baseLevel());
        entity.setIntentLevel(request.intentLevel());
        entity.setCurrentStage(request.currentStage());
        entity.setOwnerUserId(request.ownerUserId());
        entity.setAssistantUserId(request.assistantUserId());
        entity.setRemarks(request.remarks());
        entity.setAddedAt(LocalDateTime.now());
        return entity;
    }

    public static void updateEntity(CustomerEntity entity, CustomerUpdateRequest request) {
        entity.setNickname(request.nickname());
        entity.setContactValue(request.contactValue());
        entity.setSourcePlatform(request.sourcePlatform());
        entity.setExpectedMajor(request.expectedMajor());
        entity.setBaseLevel(request.baseLevel());
        entity.setIntentLevel(request.intentLevel());
        entity.setCurrentStage(request.currentStage());
        entity.setCurrentConcern(request.currentConcern());
        entity.setLatestFeedback(request.latestFeedback());
        entity.setOwnerUserId(request.ownerUserId());
        entity.setAssistantUserId(request.assistantUserId());
        entity.setRemarks(request.remarks());
    }

    public static CustomerTagVo toTagVo(CustomerTagEntity entity) {
        return new CustomerTagVo(
                entity.getId(),
                entity.getTagName(),
                entity.getTagType(),
                entity.getTagColor()
        );
    }

    private static String generateCustomerCode() {
        String datePart = LocalDate.now().format(CODE_DATE);
        int seq = ThreadLocalRandom.current().nextInt(1, 1000);
        return "C" + datePart + String.format("%03d", seq);
    }
}
