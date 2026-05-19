package com.yousells.modules.customer;

import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.modules.customer.dto.CustomerCreateRequest;
import com.yousells.modules.customer.dto.CustomerNextFollowRequest;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.dto.CustomerTagsUpdateRequest;
import com.yousells.modules.customer.dto.CustomerUpdateRequest;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.entity.CustomerTagEntity;
import com.yousells.modules.customer.entity.CustomerTagRelationEntity;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.customer.mapper.CustomerTagMapper;
import com.yousells.modules.customer.mapper.CustomerTagRelationMapper;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.CustomerDetailVo;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.customer.vo.CustomerTagVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerTagMapper customerTagMapper;

    @Autowired
    private CustomerTagRelationMapper customerTagRelationMapper;

    private Long tagId1;
    private Long tagId2;
    private Long customerId1;
    private Long customerId2;

    @BeforeEach
    void setUp() {
        CustomerTagEntity tag1 = new CustomerTagEntity();
        tag1.setTagName("竞赛意向");
        tag1.setTagType("DIRECTION");
        tag1.setTagColor("#2563eb");
        tag1.setSortOrder(1);
        customerTagMapper.insert(tag1);
        tagId1 = tag1.getId();

        CustomerTagEntity tag2 = new CustomerTagEntity();
        tag2.setTagName("零基础");
        tag2.setTagType("BASE");
        tag2.setTagColor("#16a34a");
        tag2.setSortOrder(2);
        customerTagMapper.insert(tag2);
        tagId2 = tag2.getId();

        CustomerEntity c1 = new CustomerEntity();
        c1.setCustomerCode("C20260518001");
        c1.setCustomerType("OLD_GRADE1");
        c1.setNickname("小林");
        c1.setContactValue("QQ: 10001");
        c1.setSourcePlatform("QQ");
        c1.setIntentLevel("A");
        c1.setCurrentStage("HIGH_INTENT");
        c1.setOwnerUserId(1L);
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
        c2.setOwnerUserId(2L);
        customerMapper.insert(c2);
        customerId2 = c2.getId();

        CustomerTagRelationEntity rel = new CustomerTagRelationEntity();
        rel.setCustomerId(customerId1);
        rel.setTagId(tagId1);
        customerTagRelationMapper.insert(rel);
    }

    // ── BE-005: pageCustomers & listTags ──

    @Test
    void shouldPageAllCustomers() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, null, null, null, null, null));
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.list()).hasSize(2);
    }

    @Test
    void shouldPaginateCorrectly() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 1, null, null, null, null, null));
        assertThat(result.list()).hasSize(1);
        assertThat(result.total()).isEqualTo(2);
    }

    @Test
    void shouldFilterByKeyword() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, "小林", null, null, null, null));
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list().get(0).nickname()).isEqualTo("小林");
    }

    @Test
    void shouldFilterByIntentLevel() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, null, "A", null, null, null));
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list().get(0).intentLevel()).isEqualTo("A");
    }

    @Test
    void shouldFilterByCurrentStage() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, null, null, "NURTURING", null, null));
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list().get(0).currentStage()).isEqualTo("NURTURING");
    }

    @Test
    void shouldFilterBySourcePlatform() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, null, null, null, null, "QQ"));
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.list().get(0).sourcePlatform()).isEqualTo("QQ");
    }

    @Test
    void shouldFilterByOwnerUserId() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, null, null, null, 2L, null));
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyForNoMatch() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, "不存在", null, null, null, null));
        assertThat(result.total()).isEqualTo(0);
        assertThat(result.list()).isEmpty();
    }

    @Test
    void shouldIncludeTagsInCustomerList() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, "小林", null, null, null, null));
        assertThat(result.list().get(0).tags()).containsExactly("竞赛意向");
    }

    @Test
    void shouldHaveEmptyTagsForCustomerWithoutTags() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, "阿泽", null, null, null, null));
        assertThat(result.list().get(0).tags()).isEmpty();
    }

    @Test
    void shouldListAllTags() {
        List<CustomerTagVo> tags = customerService.listTags();
        assertThat(tags).hasSizeGreaterThanOrEqualTo(2);
        assertThat(tags.stream().map(CustomerTagVo::tagName))
                .contains("竞赛意向", "零基础");
    }

    @Test
    void shouldReturnTagFieldsComplete() {
        List<CustomerTagVo> tags = customerService.listTags();
        CustomerTagVo first = tags.get(0);
        assertThat(first.id()).isNotNull();
        assertThat(first.tagName()).isNotBlank();
        assertThat(first.tagType()).isNotBlank();
        assertThat(first.tagColor()).isNotBlank();
    }

    @Test
    void shouldSetOwnerDisplayNameAsEmptyWhenUserNotFound() {
        PageResponse<CustomerListItemVo> result = customerService.pageCustomers(
                new CustomerQueryRequest(1, 10, null, null, null, null, null));
        CustomerListItemVo first = result.list().get(0);
        assertThat(first.ownerDisplayName()).isNotNull();
    }

    // ── BE-006: getCustomerDetail ──

    @Test
    void shouldGetCustomerDetail() {
        CustomerDetailVo detail = customerService.getCustomerDetail(customerId1);
        assertThat(detail.id()).isEqualTo(customerId1);
        assertThat(detail.nickname()).isEqualTo("小林");
        assertThat(detail.customerCode()).isEqualTo("C20260518001");
        assertThat(detail.intentLevel()).isEqualTo("A");
        assertThat(detail.currentStage()).isEqualTo("HIGH_INTENT");
        assertThat(detail.tags()).containsExactly("竞赛意向");
    }

    @Test
    void shouldThrowNotFoundWhenCustomerNotExists() {
        assertThatThrownBy(() -> customerService.getCustomerDetail(9999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NOT_FOUND);
    }

    // ── BE-006: createCustomer ──

    @Test
    void shouldCreateCustomer() {
        CustomerCreateRequest request = new CustomerCreateRequest(
                "新客户", "QQ: 12345", "QQ", "OLD_GRADE1",
                "软件工程", "零基础", "B", "NURTURING",
                1L, null, "测试备注");
        Long newId = customerService.createCustomer(request);
        assertThat(newId).isNotNull().isPositive();

        CustomerDetailVo created = customerService.getCustomerDetail(newId);
        assertThat(created.nickname()).isEqualTo("新客户");
        assertThat(created.contactValue()).isEqualTo("QQ: 12345");
    }

    @Test
    void shouldAutoGenerateCustomerCode() {
        CustomerCreateRequest request = new CustomerCreateRequest(
                "代码测试", "微信: test", "微信", "OLD_GRADE1",
                null, null, "C", "FIRST_COMMUNICATION",
                1L, null, null);
        Long newId = customerService.createCustomer(request);

        CustomerDetailVo created = customerService.getCustomerDetail(newId);
        assertThat(created.customerCode()).startsWith("C");
        assertThat(created.customerCode()).hasSizeGreaterThan(9);
    }

    // ── BE-006: updateCustomer ──

    @Test
    void shouldUpdateCustomer() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "小林改", "新联系方式", "微信",
                "数据科学", "有基础", "A", "HIGH_INTENT",
                "新关注点", "新反馈", 2L, null, "新备注");
        customerService.updateCustomer(customerId1, request);

        CustomerDetailVo updated = customerService.getCustomerDetail(customerId1);
        assertThat(updated.nickname()).isEqualTo("小林改");
        assertThat(updated.sourcePlatform()).isEqualTo("微信");
        assertThat(updated.expectedMajor()).isEqualTo("数据科学");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentCustomer() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "某某", "联系方式", "QQ",
                null, null, "A", "HIGH_INTENT",
                null, null, 1L, null, null);
        assertThatThrownBy(() -> customerService.updateCustomer(9999L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NOT_FOUND);
    }

    // ── BE-006: updateTags ──

    @Test
    void shouldUpdateCustomerTags() {
        customerService.updateTags(customerId1, new CustomerTagsUpdateRequest(List.of(tagId2)));

        CustomerDetailVo detail = customerService.getCustomerDetail(customerId1);
        assertThat(detail.tags()).containsExactly("零基础");
    }

    @Test
    void shouldClearAllTagsWhenEmptyList() {
        customerService.updateTags(customerId1, new CustomerTagsUpdateRequest(List.of()));

        CustomerDetailVo detail = customerService.getCustomerDetail(customerId1);
        assertThat(detail.tags()).isEmpty();
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingTagsForNonExistentCustomer() {
        assertThatThrownBy(() -> customerService.updateTags(9999L, new CustomerTagsUpdateRequest(List.of(tagId1))))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NOT_FOUND);
    }

    // ── BE-006: updateNextFollow ──

    @Test
    void shouldUpdateNextFollow() {
        LocalDateTime followTime = LocalDateTime.of(2026, 6, 1, 14, 0);
        customerService.updateNextFollow(customerId1,
                new CustomerNextFollowRequest("电话回访", followTime));

        CustomerDetailVo detail = customerService.getCustomerDetail(customerId1);
        assertThat(detail.nextFollowAction()).isEqualTo("电话回访");
        assertThat(detail.nextFollowAt()).isEqualTo(followTime);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNextFollowForNonExistentCustomer() {
        assertThatThrownBy(() -> customerService.updateNextFollow(9999L,
                new CustomerNextFollowRequest("回访", LocalDateTime.now())))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NOT_FOUND);
    }
}
