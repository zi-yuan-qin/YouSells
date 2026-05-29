package com.yousells.modules.followup.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.customer.entity.AiInsightCache;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.mapper.AiInsightCacheMapper;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.followup.convert.FollowUpConvert;
import com.yousells.modules.followup.dto.FollowUpCreateRequest;
import com.yousells.modules.followup.dto.FollowUpQueryRequest;
import com.yousells.modules.followup.entity.FollowUpEntity;
import com.yousells.modules.followup.mapper.FollowUpMapper;
import com.yousells.modules.followup.service.FollowUpService;
import com.yousells.modules.followup.vo.FollowUpVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpMapper followUpMapper;
    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final AiInsightCacheMapper aiInsightCacheMapper;

    public FollowUpServiceImpl(FollowUpMapper followUpMapper, CustomerMapper customerMapper,
                               UserMapper userMapper, AiInsightCacheMapper aiInsightCacheMapper) {
        this.followUpMapper = followUpMapper;
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
        this.aiInsightCacheMapper = aiInsightCacheMapper;
    }

    @Override
    public PageResponse<FollowUpVo> pageFollowUps(FollowUpQueryRequest request) {
        int pageNum = request.page() == null || request.page() < 1 ? 1 : request.page();
        int pageSize = request.pageSize() == null || request.pageSize() < 1 ? 20 : request.pageSize();

        List<Long> visibleUserIds = resolveVisibleUserIds();

        Page<FollowUpEntity> page = new Page<>(pageNum, pageSize);
        IPage<FollowUpEntity> result = followUpMapper.pageFollowUps(page, request.customerId(), visibleUserIds);

        List<FollowUpEntity> entities = result.getRecords();
        if (entities.isEmpty()) {
            return PageResponse.of(List.of(), pageNum, pageSize, result.getTotal());
        }

        Map<Long, String> displayNameMap = buildDisplayNameMap(entities);

        List<FollowUpVo> list = entities.stream()
                .map(e -> FollowUpConvert.toVo(e,
                        displayNameMap.getOrDefault(e.getUserId(), "")))
                .toList();

        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public Long createFollowUp(FollowUpCreateRequest request) {
        CustomerEntity customer = customerMapper.selectById(request.customerId());
        if (customer == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }

        Long userId = SecurityUserContext.requireCurrentUser().userId();
        List<Long> visibleUserIds = resolveVisibleUserIds();
        if (!visibleUserIds.contains(customer.getOwnerUserId()) && !visibleUserIds.contains(customer.getInviterUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权为该客户创建跟进记录");
        }

        FollowUpEntity entity = FollowUpConvert.toEntity(request, userId);
        followUpMapper.insert(entity);

        AiInsightCache cache = aiInsightCacheMapper.selectValidByCustomerId(request.customerId());
        if (cache != null) {
            cache.setExpiresAt(java.time.LocalDateTime.now());
            aiInsightCacheMapper.updateById(cache);
        }

        return entity.getId();
    }

    private List<Long> resolveVisibleUserIds() {
        LoginUser currentUser = SecurityUserContext.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        ids.add(currentUser.userId());
        ids.addAll(DataScopeHelper.getSubordinateIds(currentUser.userId(), userMapper));
        return ids;
    }

    private Map<Long, String> buildDisplayNameMap(List<FollowUpEntity> entities) {
        Set<Long> userIds = entities.stream()
                .map(FollowUpEntity::getUserId)
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
