package com.yousells.modules.followup;

import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.LoginUser;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.followup.dto.FollowUpCreateRequest;
import com.yousells.modules.followup.dto.FollowUpQueryRequest;
import com.yousells.modules.followup.service.FollowUpService;
import com.yousells.modules.followup.vo.FollowUpVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class FollowUpServiceTest {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private CustomerMapper customerMapper;

    private Long customerId1;
    private Long customerId2;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser(1L, "admin", "管理员", "T2", null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));

        CustomerEntity c1 = new CustomerEntity();
        c1.setCustomerCode("C20260518001");
        c1.setCustomerType("OLD_GRADE1");
        c1.setNickname("小林");
        c1.setContactValue("QQ: 10001");
        c1.setSourcePlatform("QQ");
        c1.setIntentLevel("A");
        c1.setCurrentStage("HIGH_INTENT");
        c1.setOwnerUserId(10L);
        customerMapper.insert(c1);
        customerId1 = c1.getId();

        CustomerEntity c2 = new CustomerEntity();
        c2.setCustomerCode("C20260518002");
        c2.setCustomerType("OLD_GRADE1");
        c2.setNickname("阿泽");
        c2.setContactValue("微信: az-2026");
        c2.setSourcePlatform("微信");
        c2.setIntentLevel("B");
        c2.setCurrentStage("NURTURING");
        c2.setOwnerUserId(20L);
        customerMapper.insert(c2);
        customerId2 = c2.getId();

        FollowUpCreateRequest req1 = new FollowUpCreateRequest(
                customerId1, "CHAT", "第一次沟通", "积极回应", "价格", "发送资料", null);
        followUpService.createFollowUp(req1);

        FollowUpCreateRequest req2 = new FollowUpCreateRequest(
                customerId2, "GROUP", "拉入群聊", "感兴趣", null, "安排体验", null);
        followUpService.createFollowUp(req2);
    }

    @Test
    void shouldPageAllFollowUps() {
        PageResponse<FollowUpVo> result = followUpService.pageFollowUps(
                new FollowUpQueryRequest(null, 1, 10));
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.list()).hasSize(2);
    }

    @Test
    void shouldFilterByCustomerId() {
        PageResponse<FollowUpVo> result = followUpService.pageFollowUps(
                new FollowUpQueryRequest(customerId1, 1, 10));
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list().get(0).customerId()).isEqualTo(customerId1);
    }

    @Test
    void shouldPaginateFollowUps() {
        PageResponse<FollowUpVo> result = followUpService.pageFollowUps(
                new FollowUpQueryRequest(null, 1, 1));
        assertThat(result.list()).hasSize(1);
        assertThat(result.total()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyForNoFollowUps() {
        PageResponse<FollowUpVo> result = followUpService.pageFollowUps(
                new FollowUpQueryRequest(9999L, 1, 10));
        assertThat(result.total()).isEqualTo(0);
        assertThat(result.list()).isEmpty();
    }

    @Test
    void shouldCreateFollowUp() {
        FollowUpCreateRequest request = new FollowUpCreateRequest(
                customerId1, "CHAT", "第二次沟通", "确认意向",
                "课程内容", "发课程大纲", LocalDateTime.of(2026, 6, 1, 10, 0));
        Long newId = followUpService.createFollowUp(request);
        assertThat(newId).isNotNull().isPositive();

        PageResponse<FollowUpVo> result = followUpService.pageFollowUps(
                new FollowUpQueryRequest(customerId1, 1, 10));
        assertThat(result.total()).isEqualTo(2);
    }

    @Test
    void shouldSyncCustomerOnFollowUpCreate() {
        LocalDateTime followTime = LocalDateTime.of(2026, 6, 15, 14, 0);
        FollowUpCreateRequest request = new FollowUpCreateRequest(
                customerId1, "PHONE", "电话沟通", "考虑中",
                "时间安排", "电话回访", followTime);
        followUpService.createFollowUp(request);

        CustomerEntity customer = customerMapper.selectById(customerId1);
        assertThat(customer.getLatestFeedback()).isEqualTo("电话沟通");
        assertThat(customer.getLastContactAt()).isNotNull();
        assertThat(customer.getNextFollowAction()).isEqualTo("电话回访");
        assertThat(customer.getNextFollowAt()).isEqualTo(followTime);
        assertThat(customer.getCurrentConcern()).isEqualTo("时间安排");
    }

    @Test
    void shouldThrowNotFoundWhenCustomerNotExists() {
        FollowUpCreateRequest request = new FollowUpCreateRequest(
                9999L, "CHAT", "内容", null, null, null, null);
        assertThatThrownBy(() -> followUpService.createFollowUp(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NOT_FOUND);
    }

    @Test
    void shouldSetDisplayNames() {
        PageResponse<FollowUpVo> result = followUpService.pageFollowUps(
                new FollowUpQueryRequest(null, 1, 10));
        FollowUpVo first = result.list().get(0);
        assertThat(first.operatorDisplayName()).isNotNull();
        assertThat(first.ownerDisplayName()).isNotNull();
    }
}
