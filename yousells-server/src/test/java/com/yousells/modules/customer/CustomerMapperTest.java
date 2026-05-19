package com.yousells.modules.customer;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.customer.entity.CustomerEntity;
import com.yousells.modules.customer.entity.CustomerTagEntity;
import com.yousells.modules.customer.entity.CustomerTagRelationEntity;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.customer.mapper.CustomerTagMapper;
import com.yousells.modules.customer.mapper.CustomerTagRelationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CustomerMapperTest {

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

    @Test
    void shouldQueryAllTags() {
        List<CustomerTagEntity> tags = customerTagMapper.selectList(null);
        assertThat(tags).hasSizeGreaterThanOrEqualTo(2);
        assertThat(tags.stream().map(CustomerTagEntity::getTagName))
                .contains("竞赛意向", "零基础");
    }

    @Test
    void shouldQueryTagNamesByCustomerId() {
        List<String> tagNames = customerTagRelationMapper.selectTagNamesByCustomerId(customerId1);
        assertThat(tagNames).containsExactly("竞赛意向");
    }

    @Test
    void shouldReturnEmptyTagsForCustomerWithoutTags() {
        List<String> tagNames = customerTagRelationMapper.selectTagNamesByCustomerId(customerId2);
        assertThat(tagNames).isEmpty();
    }

    @Test
    void shouldPageCustomersWithoutFilters() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), null, null, null, null, null);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getRecords()).hasSize(2);
    }

    @Test
    void shouldPageCustomersWithPageSize() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 1), null, null, null, null, null);
        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getPages()).isEqualTo(2);
    }

    @Test
    void shouldFilterByKeywordOnNickname() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), "小林", null, null, null, null);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getNickname()).isEqualTo("小林");
    }

    @Test
    void shouldFilterByKeywordOnCustomerCode() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), "C20260518002", null, null, null, null);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getCustomerCode()).isEqualTo("C20260518002");
    }

    @Test
    void shouldFilterByIntentLevel() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), null, "A", null, null, null);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getIntentLevel()).isEqualTo("A");
    }

    @Test
    void shouldFilterByCurrentStage() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), null, null, "NURTURING", null, null);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getCurrentStage()).isEqualTo("NURTURING");
    }

    @Test
    void shouldFilterBySourcePlatform() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), null, null, null, "QQ", null);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getSourcePlatform()).isEqualTo("QQ");
    }

    @Test
    void shouldFilterByOwnerUserId() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), null, null, null, null, 2L);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getOwnerUserId()).isEqualTo(2L);
    }

    @Test
    void shouldCombineFilters() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), "阿", "B", "NURTURING", "微信", 2L);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords().get(0).getNickname()).isEqualTo("阿泽");
    }

    @Test
    void shouldReturnEmptyForNonMatchingKeyword() {
        IPage<CustomerEntity> page = customerMapper.pageCustomers(
                new Page<>(1, 10), "不存在的客户", null, null, null, null);
        assertThat(page.getTotal()).isEqualTo(0);
    }

    @Test
    void shouldInsertAndQueryTagRelation() {
        CustomerTagRelationEntity rel = new CustomerTagRelationEntity();
        rel.setCustomerId(customerId2);
        rel.setTagId(tagId2);
        customerTagRelationMapper.insert(rel);

        List<String> tagNames = customerTagRelationMapper.selectTagNamesByCustomerId(customerId2);
        assertThat(tagNames).containsExactly("零基础");
    }

    @Test
    void shouldInsertAndRetrieveCustomer() {
        CustomerEntity c3 = new CustomerEntity();
        c3.setCustomerCode("C20260518003");
        c3.setCustomerType("OLD_GRADE1");
        c3.setNickname("测试客户");
        c3.setContactValue("测试联系方式");
        c3.setSourcePlatform("测试平台");
        c3.setIntentLevel("C");
        c3.setCurrentStage("FIRST_COMMUNICATION");
        c3.setOwnerUserId(3L);
        customerMapper.insert(c3);

        CustomerEntity found = customerMapper.selectById(c3.getId());
        assertThat(found).isNotNull();
        assertThat(found.getNickname()).isEqualTo("测试客户");
    }
}
