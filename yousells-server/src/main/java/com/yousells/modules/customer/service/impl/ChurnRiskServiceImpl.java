package com.yousells.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yousells.common.ai.AiService;
import com.yousells.common.constant.NotificationTypeConstants;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.customer.dto.RiskAnalysis;
import com.yousells.modules.customer.entity.ChurnRiskEntity;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.mapper.ChurnRiskMapper;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.customer.service.ChurnRiskService;
import com.yousells.modules.customer.vo.ChurnEvaluateResponse;
import com.yousells.modules.dashboard.vo.ChurnRiskItemVo;
import com.yousells.modules.dashboard.vo.ChurnRiskResponse;
import com.yousells.modules.followup.entity.FollowUpEntity;
import com.yousells.modules.followup.mapper.FollowUpMapper;
import com.yousells.modules.notification.entity.NotificationEntity;
import com.yousells.modules.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChurnRiskServiceImpl implements ChurnRiskService {

    private static final Logger log = LoggerFactory.getLogger(ChurnRiskServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final List<String> NEGATIVE_KEYWORDS = List.of(
            "联系不上", "无人接", "不回复", "找不到", "被拒", "婉拒",
            "已报名", "去了别家", "不考虑", "不用了", "没兴趣"
    );

    private final CustomerMapper customerMapper;
    private final FollowUpMapper followUpMapper;
    private final ChurnRiskMapper churnRiskMapper;
    private final AiService aiService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    public ChurnRiskServiceImpl(CustomerMapper customerMapper,
                                FollowUpMapper followUpMapper,
                                ChurnRiskMapper churnRiskMapper,
                                AiService aiService,
                                NotificationService notificationService,
                                UserMapper userMapper) {
        this.customerMapper = customerMapper;
        this.followUpMapper = followUpMapper;
        this.churnRiskMapper = churnRiskMapper;
        this.aiService = aiService;
        this.notificationService = notificationService;
        this.userMapper = userMapper;
    }

    @Override
    public ChurnEvaluateResponse evaluateAll() {
        List<CustomerEntity> allCustomers = customerMapper.listCustomers(
                null, null, null, null, null, null, null);
        List<CustomerEntity> activeCustomers = new ArrayList<>();
        for (CustomerEntity c : allCustomers) {
            if (!"成交".equals(c.getProgress())) {
                activeCustomers.add(c);
            }
        }

        int highRiskCount = 0;
        int mediumRiskCount = 0;

        for (CustomerEntity customer : activeCustomers) {
            ChurnScoreResult result = calculateScore(customer);
            if (result.riskLevel == null) {
                deleteRiskRecord(customer.getId());
                continue;
            }

            if ("high".equals(result.riskLevel)) {
                highRiskCount++;
            } else {
                mediumRiskCount++;
            }

            processRisk(customer, result);
        }

        return new ChurnEvaluateResponse(
                activeCustomers.size(),
                highRiskCount,
                mediumRiskCount,
                LocalDateTime.now()
        );
    }

    @Override
    public void evaluateCustomer(Long customerId) {
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return;
        }
        if ("成交".equals(customer.getProgress())) {
            deleteRiskRecord(customerId);
            return;
        }

        ChurnScoreResult result = calculateScore(customer);

        if (result.riskLevel == null) {
            deleteRiskRecord(customerId);
        } else {
            processRisk(customer, result);
        }
    }

    private void processRisk(CustomerEntity customer, ChurnScoreResult result) {
        List<String> finalFactors = result.factors;
        String finalSuggestion = "建议尽快跟进该客户，了解其最新需求和顾虑。";

        if ("high".equals(result.riskLevel) || "medium".equals(result.riskLevel)) {
            try {
                String userPrompt = buildAiPrompt(customer, result);
                RiskAnalysis analysis = aiService.generateStructured(
                        "你是一名销售管理专家，擅长分析客户流失风险。请输出JSON格式。",
                        userPrompt,
                        RiskAnalysis.class
                );
                if (analysis != null && analysis.getRiskFactors() != null && !analysis.getRiskFactors().isEmpty()) {
                    finalFactors = analysis.getRiskFactors();
                }
                if (analysis != null && analysis.getSuggestion() != null && !analysis.getSuggestion().isBlank()) {
                    finalSuggestion = analysis.getSuggestion();
                }
            } catch (Exception e) {
                log.warn("AI churn analysis failed for customer {}, using rule-based fallback", customer.getId(), e);
            }
        }

        upsertRisk(customer, result, finalFactors, finalSuggestion);

        if ("high".equals(result.riskLevel)) {
            sendNotification(customer, result);
        }
    }

    private void deleteRiskRecord(Long customerId) {
        churnRiskMapper.delete(
                new LambdaQueryWrapper<ChurnRiskEntity>()
                        .eq(ChurnRiskEntity::getCustomerId, customerId)
        );
    }

    @Override
    public ChurnRiskResponse getChurnRisks() {
        LoginUser currentUser = SecurityUserContext.getCurrentUser();
        List<Long> visibleUserIds = new ArrayList<>();
        if (currentUser != null) {
            visibleUserIds.add(currentUser.userId());
            visibleUserIds.addAll(DataScopeHelper.getSubordinateIds(currentUser.userId(), userMapper));
        }

        List<ChurnRiskEntity> allRisks = churnRiskMapper.selectList(
                new LambdaQueryWrapper<ChurnRiskEntity>()
        );

        // Fetch all customers via XML-based query (avoids MyBatis-Plus auto-column issues)
        List<CustomerEntity> allCustomers = customerMapper.listCustomers(
                null, null, null, null, null, null, null);
        java.util.Map<Long, CustomerEntity> customerMap = new java.util.HashMap<>();
        for (CustomerEntity c : allCustomers) {
            customerMap.put(c.getId(), c);
        }

        List<ChurnRiskItemVo> highRisk = new ArrayList<>();
        List<ChurnRiskItemVo> mediumRisk = new ArrayList<>();

        for (ChurnRiskEntity risk : allRisks) {
            CustomerEntity customer = customerMap.get(risk.getCustomerId());
            if (customer == null) {
                continue;
            }

            if (!visibleUserIds.isEmpty()
                    && !visibleUserIds.contains(customer.getOwnerUserId())
                    && !visibleUserIds.contains(customer.getInviterUserId())) {
                continue;
            }

            ChurnRiskItemVo item = buildItemVo(risk, customer);
            if ("high".equals(risk.getRiskLevel())) {
                highRisk.add(item);
            } else if ("medium".equals(risk.getRiskLevel())) {
                mediumRisk.add(item);
            }
        }

        LocalDateTime latestEval = allRisks.stream()
                .map(ChurnRiskEntity::getEvaluatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new ChurnRiskResponse(highRisk, mediumRisk, highRisk.size() + mediumRisk.size(), latestEval);
    }

    private ChurnScoreResult calculateScore(CustomerEntity customer) {
        int score = 0;
        List<String> factors = new ArrayList<>();

        FollowUpEntity latestFollowUp = followUpMapper.selectList(
                new LambdaQueryWrapper<FollowUpEntity>()
                        .eq(FollowUpEntity::getCustomerId, customer.getId())
                        .orderByDesc(FollowUpEntity::getCreatedAt)
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);

        long silentDays;
        if (latestFollowUp != null && latestFollowUp.getCreatedAt() != null) {
            silentDays = ChronoUnit.DAYS.between(latestFollowUp.getCreatedAt(), LocalDateTime.now());
        } else if (customer.getCreatedAt() != null) {
            silentDays = ChronoUnit.DAYS.between(customer.getCreatedAt(), LocalDateTime.now());
        } else {
            silentDays = 0;
        }

        if (silentDays > 14) {
            score += 40;
            factors.add("沉默超过14天");
        } else if (silentDays > 7) {
            score += 20;
            factors.add("沉默超过7天");
        }

        String intent = customer.getIntent();
        if ("冷淡".equals(intent)) {
            score += 25;
            factors.add("意向冷淡");
        } else if ("观望".equals(intent)) {
            score += 25;
            factors.add("意向下降(观望)");
        }

        if (latestFollowUp != null && latestFollowUp.getContent() != null) {
            String content = latestFollowUp.getContent();
            for (String kw : NEGATIVE_KEYWORDS) {
                if (content.contains(kw)) {
                    score += 20;
                    factors.add("跟进内容含消极关键词");
                    break;
                }
            }
        }

        long followUpCount30d = followUpMapper.selectCount(
                new LambdaQueryWrapper<FollowUpEntity>()
                        .eq(FollowUpEntity::getCustomerId, customer.getId())
                        .ge(FollowUpEntity::getCreatedAt, LocalDateTime.now().minusDays(30))
        );
        if (followUpCount30d == 0) {
            score += 20;
            factors.add("30天无跟进");
        }

        if (latestFollowUp != null
                && latestFollowUp.getNextAction() != null
                && !latestFollowUp.getNextAction().isBlank()
                && latestFollowUp.getCreatedAt() != null
                && latestFollowUp.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
            score += 10;
            factors.add("下一步行动计划逾期7天+");
        }

        String riskLevel;
        if (score >= 60) {
            riskLevel = "high";
        } else if (score >= 30) {
            riskLevel = "medium";
        } else {
            riskLevel = null;
        }

        ChurnScoreResult result = new ChurnScoreResult();
        result.score = score;
        result.riskLevel = riskLevel;
        result.factors = factors;
        result.silentDays = (int) silentDays;
        result.latestFollowUp = latestFollowUp;
        return result;
    }

    private String buildAiPrompt(CustomerEntity customer, ChurnScoreResult result) {
        String latestContent = "无跟进记录";
        if (result.latestFollowUp != null && result.latestFollowUp.getContent() != null) {
            latestContent = result.latestFollowUp.getContent();
        }

        return String.format(
                "客户姓名：%s\n年级：%s\n专业：%s\n当前意向：%s\n当前进度：%s\n" +
                        "沉默天数：%d\n风险评分：%d分（满分100）\n" +
                        "规则检测到的风险因素：%s\n" +
                        "最近跟进内容：%s\n\n" +
                        "请分析该客户的流失风险并给出具体干预建议。",
                customer.getRealName() != null ? customer.getRealName() : "客户#" + customer.getId(),
                customer.getGrade() != null ? customer.getGrade() : "未知",
                customer.getMajor() != null ? customer.getMajor() : "未知",
                customer.getIntent() != null ? customer.getIntent() : "未知",
                customer.getProgress() != null ? customer.getProgress() : "未知",
                result.silentDays,
                result.score,
                String.join("、", result.factors),
                latestContent
        );
    }

    private void upsertRisk(CustomerEntity customer, ChurnScoreResult result,
                            List<String> factors, String suggestion) {
        ChurnRiskEntity existing = churnRiskMapper.selectOne(
                new LambdaQueryWrapper<ChurnRiskEntity>()
                        .eq(ChurnRiskEntity::getCustomerId, customer.getId())
        );

        if (existing != null) {
            existing.setRiskLevel(result.riskLevel);
            existing.setRiskScore(result.score);
            existing.setRiskFactors(toJsonArray(factors));
            existing.setSuggestion(suggestion);
            existing.setEvaluatedAt(LocalDateTime.now());
            churnRiskMapper.updateById(existing);
        } else {
            ChurnRiskEntity entity = new ChurnRiskEntity();
            entity.setCustomerId(customer.getId());
            entity.setRiskLevel(result.riskLevel);
            entity.setRiskScore(result.score);
            entity.setRiskFactors(toJsonArray(factors));
            entity.setSuggestion(suggestion);
            entity.setEvaluatedAt(LocalDateTime.now());
            churnRiskMapper.insert(entity);
        }
    }

    private void sendNotification(CustomerEntity customer, ChurnScoreResult result) {
        String customerName = customer.getRealName() != null ? customer.getRealName() : "ID:" + customer.getId();
        String content = "客户「" + customerName + "」存在流失风险（评分" + result.score + "分），建议尽快跟进。";

        // 发给负责人
        sendSingleNotification(customer.getOwnerUserId(), content, customer.getId());

        // 发给邀约人（如果与负责人不同）
        if (customer.getInviterUserId() != null
                && !customer.getInviterUserId().equals(customer.getOwnerUserId())) {
            sendSingleNotification(customer.getInviterUserId(), content, customer.getId());
        }
    }

    private void sendSingleNotification(Long userId, String content, Long businessId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setType(NotificationTypeConstants.FOLLOW_UP_REMINDER);
        notification.setTitle("流失预警");
        notification.setContent(content);
        notification.setBusinessType("customer");
        notification.setBusinessId(businessId);
        notificationService.sendNotification(notification);
    }

    private ChurnRiskItemVo buildItemVo(ChurnRiskEntity risk, CustomerEntity customer) {
        int silentDays = 0;
        LocalDateTime lastFollowUpAt = null;

        FollowUpEntity latest = followUpMapper.selectList(
                new LambdaQueryWrapper<FollowUpEntity>()
                        .eq(FollowUpEntity::getCustomerId, customer.getId())
                        .orderByDesc(FollowUpEntity::getCreatedAt)
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);

        if (latest != null && latest.getCreatedAt() != null) {
            silentDays = (int) ChronoUnit.DAYS.between(latest.getCreatedAt(), LocalDateTime.now());
            lastFollowUpAt = latest.getCreatedAt();
        } else if (customer.getCreatedAt() != null) {
            silentDays = (int) ChronoUnit.DAYS.between(customer.getCreatedAt(), LocalDateTime.now());
        }

        List<String> factorsList = parseJsonArray(risk.getRiskFactors());

        String customerName = customer.getRealName() != null ? customer.getRealName() : "客户#" + customer.getId();

        return new ChurnRiskItemVo(
                customer.getId(),
                customerName,
                risk.getRiskLevel(),
                risk.getRiskScore(),
                factorsList,
                risk.getSuggestion() != null ? risk.getSuggestion() : "建议尽快跟进该客户。",
                silentDays,
                lastFollowUpAt,
                risk.getEvaluatedAt()
        );
    }

    private static class ChurnScoreResult {
        int score;
        String riskLevel;
        List<String> factors;
        int silentDays;
        FollowUpEntity latestFollowUp;
    }

    private static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) return "[]";
        try {
            return OBJECT_MAPPER.writeValueAsString(items);
        } catch (Exception e) {
            log.warn("Failed to serialize risk factors to JSON", e);
            return "[]";
        }
    }

    private static List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse risk factors JSON", e);
            return List.of();
        }
    }
}
