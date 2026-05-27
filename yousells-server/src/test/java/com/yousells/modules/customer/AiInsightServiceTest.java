package com.yousells.modules.customer;

import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.security.LoginUser;
import com.yousells.modules.auth.entity.UserEntity;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.customer.dto.AiInsightResponse;
import com.yousells.modules.customer.dto.CustomerCreateRequest;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.followup.dto.FollowUpCreateRequest;
import com.yousells.modules.followup.service.FollowUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class AiInsightServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private UserMapper userMapper;

    private Long customerWithFollowUps;
    private Long customerWithoutFollowUps;
    private Long userId1;

    @BeforeEach
    void setUp() {
        UserEntity u1 = new UserEntity();
        u1.setUsername("ai_admin");
        u1.setPasswordHash("hash");
        u1.setRealName("志明");
        u1.setLevel("T2");
        u1.setStatus("ACTIVE");
        userMapper.insert(u1);
        userId1 = u1.getId();

        UserEntity u2 = new UserEntity();
        u2.setUsername("ai_member");
        u2.setPasswordHash("hash");
        u2.setRealName("小赵");
        u2.setLevel("T0");
        u2.setManagerUserId(userId1);
        u2.setStatus("ACTIVE");
        userMapper.insert(u2);

        LoginUser loginUser = new LoginUser(userId1, "ai_admin", "志明", "T2", null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));

        CustomerCreateRequest req1 = new CustomerCreateRequest(
                "张三", "大二", "计算机科学", "计科2401", userId1, userId1,
                "技术栈", "可跟", "校园地推");
        customerWithFollowUps = customerService.createCustomer(req1);

        CustomerCreateRequest req2 = new CustomerCreateRequest(
                "李四", "大三", "软件工程", null, userId1, userId1,
                "职规", "观望", null);
        customerWithoutFollowUps = customerService.createCustomer(req2);

        for (int i = 1; i <= 5; i++) {
            followUpService.createFollowUp(new FollowUpCreateRequest(
                    customerWithFollowUps, "技术栈",
                    "第" + i + "次沟通：讨论了课程内容和就业前景，客户对价格比较关注",
                    "客户反馈积极", "安排试听课"));
        }
    }

    @Test
    void shouldReturnInsightForCustomerWithFollowUps() {
        AiInsightResponse insight = customerService.getAiInsight(customerWithFollowUps);
        assertThat(insight).isNotNull();
        assertThat(insight.customerId()).isEqualTo(customerWithFollowUps);
        assertThat(insight.generatedAt()).isNotNull();
    }

    @Test
    void shouldReturnEmptyForCustomerWithoutFollowUps() {
        AiInsightResponse insight = customerService.getAiInsight(customerWithoutFollowUps);
        assertThat(insight).isNotNull();
        assertThat(insight.customerId()).isEqualTo(customerWithoutFollowUps);
        assertThat(insight.conversionConfidence()).isZero();
        assertThat(insight.summary()).contains("暂无足够跟进记录");
    }

    @Test
    void shouldCacheAndReturnCachedInsight() {
        AiInsightResponse first = customerService.getAiInsight(customerWithFollowUps);
        AiInsightResponse second = customerService.getAiInsight(customerWithFollowUps);
        assertThat(second).isNotNull();
        assertThat(second.customerId()).isEqualTo(first.customerId());
        assertThat(second.generatedAt()).isNotNull();
    }

    @Test
    void shouldRefreshInsight() {
        AiInsightResponse first = customerService.getAiInsight(customerWithFollowUps);
        AiInsightResponse refreshed = customerService.refreshAiInsight(customerWithFollowUps);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.customerId()).isEqualTo(customerWithFollowUps);
        assertThat(refreshed.generatedAt()).isNotNull();
    }

    @Test
    void shouldThrowNotFoundWhenCustomerNotExists() {
        assertThatThrownBy(() -> customerService.getAiInsight(99999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NOT_FOUND);
    }

    @Test
    void shouldForbidAccessForOutOfScopeUser() {
        UserEntity u3 = new UserEntity();
        u3.setUsername("ai_stranger");
        u3.setPasswordHash("hash");
        u3.setRealName("陌生人");
        u3.setLevel("T0");
        u3.setStatus("ACTIVE");
        userMapper.insert(u3);

        LoginUser strangerUser = new LoginUser(u3.getId(), "ai_stranger", "陌生人", "T0", null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(strangerUser, null, List.of()));

        assertThatThrownBy(() -> customerService.getAiInsight(customerWithFollowUps))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.FORBIDDEN);
    }

    @Test
    void shouldInvalidateCacheAfterNewFollowUp() {
        AiInsightResponse first = customerService.getAiInsight(customerWithFollowUps);
        assertThat(first).isNotNull();

        followUpService.createFollowUp(new FollowUpCreateRequest(
                customerWithFollowUps, "课程",
                "新增跟进：客户咨询了课程套餐和优惠活动",
                "客户对价格比较敏感", "发送优惠方案"));

        AiInsightResponse afterNewFollowUp = customerService.getAiInsight(customerWithFollowUps);
        assertThat(afterNewFollowUp).isNotNull();
        assertThat(afterNewFollowUp.generatedAt()).isNotNull();
    }
}
