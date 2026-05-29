package com.yousells.modules.customer.service;

import com.yousells.common.response.PageResponse;
import com.yousells.modules.customer.dto.AiInsightResponse;
import com.yousells.modules.customer.dto.CustomerCreateRequest;

import java.util.List;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.dto.CustomerUpdateRequest;
import com.yousells.modules.customer.vo.CustomerDetailVo;
import com.yousells.modules.customer.vo.CustomerListItemVo;

public interface CustomerService {

    PageResponse<CustomerListItemVo> pageCustomers(CustomerQueryRequest request);

    List<CustomerListItemVo> listAllCustomers(CustomerQueryRequest request);

    CustomerDetailVo getCustomerDetail(Long id);

    Long createCustomer(CustomerCreateRequest request);

    void updateCustomer(Long id, CustomerUpdateRequest request);

    AiInsightResponse getAiInsight(Long id);

    AiInsightResponse refreshAiInsight(Long id);
}
