package com.yousells.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.modules.customer.convert.CustomerConvert;
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
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerTagMapper customerTagMapper;
    private final CustomerTagRelationMapper customerTagRelationMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper,
                               CustomerTagMapper customerTagMapper,
                               CustomerTagRelationMapper customerTagRelationMapper) {
        this.customerMapper = customerMapper;
        this.customerTagMapper = customerTagMapper;
        this.customerTagRelationMapper = customerTagRelationMapper;
    }

    @Override
    public PageResponse<CustomerListItemVo> pageCustomers(CustomerQueryRequest request) {
        int pageNum = request.page() == null || request.page() < 1 ? 1 : request.page();
        int pageSize = request.pageSize() == null || request.pageSize() < 1 ? 20 : request.pageSize();

        Page<CustomerEntity> page = new Page<>(pageNum, pageSize);
        IPage<CustomerEntity> result = customerMapper.pageCustomers(page,
                request.keyword(), request.intentLevel(), request.currentStage(),
                request.sourcePlatform(), request.ownerUserId());

        List<CustomerEntity> entities = result.getRecords();
        if (entities.isEmpty()) {
            return PageResponse.of(List.of(), pageNum, pageSize, result.getTotal());
        }

        Map<Long, String> ownerNameMap = buildOwnerNameMap(entities);
        Map<Long, List<String>> tagsMap = buildTagsMap(entities);

        List<CustomerListItemVo> list = entities.stream()
                .map(e -> CustomerConvert.toListItemVo(e,
                        ownerNameMap.getOrDefault(e.getOwnerUserId(), ""),
                        tagsMap.getOrDefault(e.getId(), List.of())))
                .toList();

        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public List<CustomerTagVo> listTags() {
        List<CustomerTagEntity> entities = customerTagMapper.selectList(null);
        return entities.stream()
                .map(CustomerConvert::toTagVo)
                .toList();
    }

    @Override
    public CustomerDetailVo getCustomerDetail(Long id) {
        CustomerEntity entity = customerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }

        List<String> tags = customerTagRelationMapper.selectTagNamesByCustomerId(id);
        String ownerDisplayName = lookupDisplayName(entity.getOwnerUserId());
        String assistantDisplayName = entity.getAssistantUserId() != null
                ? lookupDisplayName(entity.getAssistantUserId())
                : null;

        return CustomerConvert.toDetailVo(entity, ownerDisplayName, assistantDisplayName, tags);
    }

    @Override
    public Long createCustomer(CustomerCreateRequest request) {
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
        CustomerConvert.updateEntity(entity, request);
        customerMapper.updateById(entity);
    }

    @Override
    public void updateTags(Long id, CustomerTagsUpdateRequest request) {
        CustomerEntity entity = customerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }

        LambdaQueryWrapper<CustomerTagRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerTagRelationEntity::getCustomerId, id);
        customerTagRelationMapper.delete(wrapper);

        for (Long tagId : request.tagIds()) {
            CustomerTagRelationEntity rel = new CustomerTagRelationEntity();
            rel.setCustomerId(id);
            rel.setTagId(tagId);
            customerTagRelationMapper.insert(rel);
        }
    }

    @Override
    public void updateNextFollow(Long id, CustomerNextFollowRequest request) {
        CustomerEntity entity = customerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "customer not found");
        }
        entity.setNextFollowAction(request.nextAction());
        entity.setNextFollowAt(request.nextFollowAt());
        customerMapper.updateById(entity);
    }

    // ── private helpers ──

    private Map<Long, String> buildOwnerNameMap(List<CustomerEntity> entities) {
        Set<Long> ownerIds = entities.stream()
                .map(CustomerEntity::getOwnerUserId)
                .collect(Collectors.toSet());
        if (ownerIds.isEmpty()) {
            return Map.of();
        }
        return customerMapper.selectOwnerDisplayNames(List.copyOf(ownerIds)).stream()
                .collect(Collectors.toMap(
                        CustomerMapper.OwnerDisplayName::getUserId,
                        CustomerMapper.OwnerDisplayName::getDisplayName,
                        (a, b) -> a));
    }

    private Map<Long, List<String>> buildTagsMap(List<CustomerEntity> entities) {
        List<Long> customerIds = entities.stream()
                .map(CustomerEntity::getId)
                .toList();
        if (customerIds.isEmpty()) {
            return Map.of();
        }
        return customerTagRelationMapper.selectTagNamesByCustomerIds(customerIds).stream()
                .collect(Collectors.groupingBy(
                        CustomerTagRelationMapper.CustomerTagMapping::getCustomerId,
                        Collectors.mapping(CustomerTagRelationMapper.CustomerTagMapping::getTagName,
                                Collectors.toList())));
    }

    private String lookupDisplayName(Long userId) {
        List<CustomerMapper.OwnerDisplayName> names = customerMapper.selectOwnerDisplayNames(List.of(userId));
        return names.isEmpty() ? "" : names.get(0).getDisplayName();
    }
}
