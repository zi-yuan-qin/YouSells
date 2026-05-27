package com.yousells.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yousells.common.ai.AiException;
import com.yousells.common.ai.AiService;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.customer.convert.CustomerConvert;
import com.yousells.modules.customer.dto.AiInsightResponse;
import com.yousells.modules.customer.dto.CustomerCreateRequest;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.dto.CustomerUpdateRequest;
import com.yousells.modules.customer.entity.AiInsightCache;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.mapper.AiInsightCacheMapper;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.CustomerDetailVo;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.followup.entity.FollowUpEntity;
import com.yousells.modules.followup.mapper.FollowUpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private static final String INSIGHT_SYSTEM_PROMPT = """
            你是一名资深的销售教练，擅长分析客户沟通记录。
            根据客户基本信息和跟进历史，输出结构化的客户洞察报告。
            只输出 JSON，不要额外文字。
            JSON 字段说明：
            - intentTrend: 意向趋势，只能是 "上升"、"平稳"、"下降" 之一
            - intentTrendReason: 意向趋势的原因分析
            - keyConcerns: 客户关键关注点，字符串数组
            - communicationStyle: 沟通风格画像
            - conversionProbability: 转化概率，只能是 "高"、"中"、"低" 之一
            - conversionConfidence: 置信度，0-100 的整数
            - nextActionSuggestion: 下一步沟通建议
            - summary: 一句话总结客户洞察
            """;

    private static final int MAX_FOLLOW_UPS_FOR_PROMPT = 20;

    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final AiService aiService;
    private final AiInsightCacheMapper aiInsightCacheMapper;
    private final FollowUpMapper followUpMapper;
    private final ObjectMapper objectMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper, UserMapper userMapper,
                               AiService aiService, AiInsightCacheMapper aiInsightCacheMapper,
                               FollowUpMapper followUpMapper, ObjectMapper objectMapper) {
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
        this.aiService = aiService;
        this.aiInsightCacheMapper = aiInsightCacheMapper;
        this.followUpMapper = followUpMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<CustomerListItemVo> pageCustomers(CustomerQueryRequest request) {
        int pageNum = request.page() == null || request.page() < 1 ? 1 : request.page();
        int pageSize = request.pageSize() == null || request.pageSize() < 1 ? 20 : request.pageSize();

        List<Long> visibleUserIds = resolveVisibleUserIds();

        Page<CustomerEntity> page = new Page<>(pageNum, pageSize);
        IPage<CustomerEntity> result = customerMapper.pageCustomers(page,
                request.keyword(), request.grade(), request.major(),
                request.progress(), request.intent(), request.ownerUserId(),
                visibleUserIds);

        List<CustomerEntity> entities = result.getRecords();
        if (entities.isEmpty()) {
            return PageResponse.of(List.of(), pageNum, pageSize, result.getTotal());
        }

        Map<Long, String> displayNameMap = buildDisplayNameMap(entities);

        List<CustomerListItemVo> list = entities.stream()
                .map(e -> CustomerConvert.toListItemVo(e,
                        displayNameMap.getOrDefault(e.getOwnerUserId(), ""),
                        displayNameMap.getOrDefault(e.getInviterUserId(), "")))
                .toList();

        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public List<CustomerListItemVo> listAllCustomers(CustomerQueryRequest request) {
        List<Long> visibleUserIds = resolveVisibleUserIds();

        List<CustomerEntity> entities = customerMapper.listCustomers(
                request.keyword(), request.grade(), request.major(),
                request.progress(), request.intent(), request.ownerUserId(),
                visibleUserIds);

        if (entities.isEmpty()) {
            return List.of();
        }

        Map<Long, String> displayNameMap = buildDisplayNameMap(entities);

        return entities.stream()
                .map(e -> CustomerConvert.toListItemVo(e,
                        displayNameMap.getOrDefault(e.getOwnerUserId(), ""),
                        displayNameMap.getOrDefault(e.getInviterUserId(), "")))
                .toList();
    }

    @Override
    public CustomerDetailVo getCustomerDetail(Long id) {
        CustomerEntity entity = customerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }
        List<Long> visibleUserIds = resolveVisibleUserIds();
        if (!visibleUserIds.contains(entity.getOwnerUserId()) && !visibleUserIds.contains(entity.getInviterUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权查看该客户");
        }

        String ownerDisplayName = lookupDisplayName(entity.getOwnerUserId());
        String inviterDisplayName = lookupDisplayName(entity.getInviterUserId());

        return CustomerConvert.toDetailVo(entity, ownerDisplayName, inviterDisplayName);
    }

    @Override
    public Long createCustomer(CustomerCreateRequest request) {
        LoginUser currentUser = SecurityUserContext.requireCurrentUser();
        List<Long> visibleUserIds = resolveVisibleUserIds();

        Long ownerUserId = request.ownerUserId() != null ? request.ownerUserId() : currentUser.userId();
        Long inviterUserId = request.inviterUserId() != null ? request.inviterUserId() : currentUser.userId();

        if (!visibleUserIds.contains(ownerUserId)) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "指定的归属人不在可操作范围内");
        }
        if (!visibleUserIds.contains(inviterUserId)) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "指定的邀约人不在可操作范围内");
        }

        CustomerEntity entity = CustomerConvert.toEntity(request);
        customerMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateCustomer(Long id, CustomerUpdateRequest request) {
        CustomerEntity entity = customerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }
        List<Long> visibleUserIds = resolveVisibleUserIds();
        if (!visibleUserIds.contains(entity.getOwnerUserId()) && !visibleUserIds.contains(entity.getInviterUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权修改该客户");
        }
        CustomerConvert.updateEntity(entity, request);
        customerMapper.updateById(entity);
    }

    @Override
    public AiInsightResponse getAiInsight(Long id) {
        return doGetAiInsight(id, false);
    }

    @Override
    public AiInsightResponse refreshAiInsight(Long id) {
        return doGetAiInsight(id, true);
    }

    private AiInsightResponse doGetAiInsight(Long id, boolean forceRefresh) {
        CustomerEntity customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }
        List<Long> visibleUserIds = resolveVisibleUserIds();
        if (!visibleUserIds.contains(customer.getOwnerUserId()) && !visibleUserIds.contains(customer.getInviterUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权查看该客户");
        }

        if (!forceRefresh) {
            AiInsightCache cached = aiInsightCacheMapper.selectValidByCustomerId(id);
            if (cached != null) {
                try {
                    AiInsightResponse response = objectMapper.readValue(cached.getInsightJson(), AiInsightResponse.class);
                    return withTimestamp(response, cached.getGeneratedAt());
                } catch (JsonProcessingException e) {
                    log.warn("Failed to deserialize insight cache for customer {}", id, e);
                }
            }
        }

        List<FollowUpEntity> followUps = queryFollowUps(id);
        if (followUps.isEmpty()) {
            return AiInsightResponse.empty(id);
        }

        String userPrompt = buildUserPrompt(customer, followUps);

        try {
            AiInsightResponse generated = aiService.generateStructured(INSIGHT_SYSTEM_PROMPT, userPrompt, AiInsightResponse.class);
            LocalDateTime now = LocalDateTime.now().withNano(0);
            AiInsightResponse response = enrichResponse(generated, id, customer, followUps, now);
            saveCache(id, response, now);
            return response;
        } catch (AiException e) {
            log.error("AI insight generation failed for customer {}", id, e);
            AiInsightCache oldCache = aiInsightCacheMapper.selectValidByCustomerId(id);
            if (oldCache != null) {
                try {
                    return objectMapper.readValue(oldCache.getInsightJson(), AiInsightResponse.class);
                } catch (JsonProcessingException ex) {
                    log.warn("Failed to read fallback cache", ex);
                }
            }
            throw new BusinessException(ErrorCodeConstants.INTERNAL_ERROR, "AI 分析暂时不可用，请稍后重试");
        }
    }

    private List<FollowUpEntity> queryFollowUps(Long customerId) {
        Page<FollowUpEntity> page = new Page<>(1, MAX_FOLLOW_UPS_FOR_PROMPT);
        List<Long> allUserIds = resolveVisibleUserIds();
        IPage<FollowUpEntity> result = followUpMapper.pageFollowUps(page, customerId, null);
        if (result.getRecords().isEmpty()) {
            return List.of();
        }
        return result.getRecords().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    private String buildUserPrompt(CustomerEntity customer, List<FollowUpEntity> followUps) {
        StringBuilder sb = new StringBuilder();
        sb.append("客户基本信息：\n");
        sb.append("- 姓名：").append(customer.getRealName()).append("\n");
        sb.append("- 年级：").append(customer.getGrade()).append("\n");
        sb.append("- 专业：").append(customer.getMajor()).append("\n");
        sb.append("- 当前进度：").append(customer.getProgress()).append("\n");
        sb.append("- 意向程度：").append(customer.getIntent()).append("\n");
        sb.append("\n跟进记录（最近").append(Math.min(followUps.size(), MAX_FOLLOW_UPS_FOR_PROMPT)).append("条）：\n");

        List<FollowUpEntity> limited = followUps.stream()
                .limit(MAX_FOLLOW_UPS_FOR_PROMPT)
                .toList();
        for (int i = 0; i < limited.size(); i++) {
            FollowUpEntity f = limited.get(i);
            sb.append(i + 1).append(". [").append(f.getCreatedAt()).append("] ");
            sb.append("进度:").append(f.getProgress()).append(" ");
            sb.append("内容:").append(f.getContent());
            if (f.getNextAction() != null && !f.getNextAction().isEmpty()) {
                sb.append(" 下一步:").append(f.getNextAction());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private AiInsightResponse enrichResponse(AiInsightResponse generated, Long customerId,
                                             CustomerEntity customer, List<FollowUpEntity> followUps,
                                             LocalDateTime generatedAt) {
        return new AiInsightResponse(
                customerId,
                generated.intentTrend() != null ? generated.intentTrend() : "平稳",
                generated.intentTrendReason() != null ? generated.intentTrendReason() : "",
                generated.keyConcerns() != null ? generated.keyConcerns() : List.of(),
                generated.communicationStyle() != null ? generated.communicationStyle() : "未知",
                generated.conversionProbability() != null ? generated.conversionProbability() : "低",
                generated.conversionConfidence() != null ? generated.conversionConfidence() : 0,
                generated.nextActionSuggestion() != null ? generated.nextActionSuggestion() : "",
                generated.summary() != null ? generated.summary() : "",
                generatedAt
        );
    }

    private AiInsightResponse withTimestamp(AiInsightResponse cached, LocalDateTime generatedAt) {
        return new AiInsightResponse(
                cached.customerId(), cached.intentTrend(), cached.intentTrendReason(),
                cached.keyConcerns(), cached.communicationStyle(), cached.conversionProbability(),
                cached.conversionConfidence(), cached.nextActionSuggestion(), cached.summary(),
                generatedAt != null ? generatedAt.withNano(0) : null
        );
    }

    private void saveCache(Long customerId, AiInsightResponse response, LocalDateTime generatedAt) {
        try {
            String json = objectMapper.writeValueAsString(response);
            AiInsightCache existing = aiInsightCacheMapper.selectValidByCustomerId(customerId);
            if (existing != null) {
                existing.setInsightJson(json);
                existing.setGeneratedAt(generatedAt);
                existing.setExpiresAt(generatedAt.plusHours(2));
                aiInsightCacheMapper.updateById(existing);
            } else {
                aiInsightCacheMapper.deleteByCustomerId(customerId);
                AiInsightCache cache = new AiInsightCache();
                cache.setCustomerId(customerId);
                cache.setInsightJson(json);
                cache.setGeneratedAt(generatedAt);
                cache.setExpiresAt(generatedAt.plusHours(2));
                aiInsightCacheMapper.insert(cache);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize insight for customer {}", customerId, e);
        }
    }

    private List<Long> resolveVisibleUserIds() {
        LoginUser currentUser = SecurityUserContext.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        ids.add(currentUser.userId());
        ids.addAll(DataScopeHelper.getSubordinateIds(currentUser.userId(), userMapper));
        return ids;
    }

    private Map<Long, String> buildDisplayNameMap(List<CustomerEntity> entities) {
        Set<Long> userIds = entities.stream()
                .flatMap(e -> java.util.stream.Stream.of(e.getOwnerUserId(), e.getInviterUserId()))
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return customerMapper.selectUserDisplayNames(List.copyOf(userIds)).stream()
                .collect(Collectors.toMap(
                        CustomerMapper.UserDisplayName::getUserId,
                        CustomerMapper.UserDisplayName::getDisplayName,
                        (a, b) -> a));
    }

    private String lookupDisplayName(Long userId) {
        List<CustomerMapper.UserDisplayName> names = customerMapper.selectUserDisplayNames(List.of(userId));
        return names.isEmpty() ? "" : names.get(0).getDisplayName();
    }
}
