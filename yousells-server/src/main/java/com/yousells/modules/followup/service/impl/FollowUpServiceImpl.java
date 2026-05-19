package com.yousells.modules.followup.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.followup.convert.FollowUpConvert;
import com.yousells.modules.followup.dto.FollowUpCreateRequest;
import com.yousells.modules.followup.dto.FollowUpQueryRequest;
import com.yousells.modules.followup.entity.FollowUpEntity;
import com.yousells.modules.followup.mapper.FollowUpMapper;
import com.yousells.modules.followup.service.FollowUpService;
import com.yousells.modules.followup.vo.FollowUpVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpMapper followUpMapper;
    private final CustomerMapper customerMapper;

    public FollowUpServiceImpl(FollowUpMapper followUpMapper, CustomerMapper customerMapper) {
        this.followUpMapper = followUpMapper;
        this.customerMapper = customerMapper;
    }

    @Override
    public PageResponse<FollowUpVo> pageFollowUps(FollowUpQueryRequest request) {
        int pageNum = request.page() == null || request.page() < 1 ? 1 : request.page();
        int pageSize = request.pageSize() == null || request.pageSize() < 1 ? 20 : request.pageSize();

        Page<FollowUpEntity> page = new Page<>(pageNum, pageSize);
        IPage<FollowUpEntity> result = followUpMapper.pageFollowUps(page, request.customerId());

        List<FollowUpEntity> entities = result.getRecords();
        if (entities.isEmpty()) {
            return PageResponse.of(List.of(), pageNum, pageSize, result.getTotal());
        }

        Map<Long, String> displayNameMap = buildDisplayNameMap(entities);

        List<FollowUpVo> list = entities.stream()
                .map(e -> FollowUpConvert.toVo(e,
                        displayNameMap.getOrDefault(e.getOperatorUserId(), ""),
                        displayNameMap.getOrDefault(e.getOwnerUserId(), "")))
                .toList();

        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public Long createFollowUp(FollowUpCreateRequest request) {
        CustomerEntity customer = customerMapper.selectById(request.customerId());
        if (customer == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }

        Long operatorUserId = SecurityUserContext.requireCurrentUser().userId();

        FollowUpEntity entity = FollowUpConvert.toEntity(request);
        entity.setOperatorUserId(operatorUserId);
        entity.setOwnerUserId(customer.getOwnerUserId());
        followUpMapper.insert(entity);

        customer.setLastContactAt(LocalDateTime.now());
        customer.setLatestFeedback(request.communicatedContent());
        if (request.nextAction() != null) {
            customer.setNextFollowAction(request.nextAction());
        }
        if (request.nextFollowAt() != null) {
            customer.setNextFollowAt(request.nextFollowAt());
        }
        if (request.currentConcern() != null) {
            customer.setCurrentConcern(request.currentConcern());
        }
        customerMapper.updateById(customer);

        return entity.getId();
    }

    private Map<Long, String> buildDisplayNameMap(List<FollowUpEntity> entities) {
        Set<Long> userIds = entities.stream()
                .flatMap(e -> Stream.of(e.getOperatorUserId(), e.getOwnerUserId()))
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return followUpMapper.selectUserDisplayNames(List.copyOf(userIds)).stream()
                .collect(Collectors.toMap(
                        FollowUpMapper.UserDisplayName::getUserId,
                        FollowUpMapper.UserDisplayName::getDisplayName,
                        (a, b) -> a));
    }
}
