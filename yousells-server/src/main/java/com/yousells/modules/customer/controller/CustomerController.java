package com.yousells.modules.customer.controller;

import com.alibaba.excel.EasyExcel;
import com.yousells.common.response.ApiResponse;
import com.yousells.common.response.IdResponse;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.customer.dto.CustomerCreateRequest;
import com.yousells.modules.customer.dto.CustomerImportDto;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.dto.CustomerUpdateRequest;
import com.yousells.modules.customer.listener.CustomerImportListener;
import com.yousells.modules.customer.mapper.CustomerMapper;
import com.yousells.modules.customer.service.ChurnRiskService;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.ChurnEvaluateResponse;
import com.yousells.modules.customer.vo.CustomerDetailVo;
import com.yousells.modules.customer.vo.CustomerExportVo;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final ChurnRiskService churnRiskService;

    public CustomerController(CustomerService customerService, CustomerMapper customerMapper, ChurnRiskService churnRiskService) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
        this.churnRiskService = churnRiskService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CustomerListItemVo>> page(CustomerQueryRequest request) {
        return ApiResponse.success(customerService.pageCustomers(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDetailVo> detail(@PathVariable Long id) {
        return ApiResponse.success(customerService.getCustomerDetail(id));
    }

    @PostMapping
    public ApiResponse<IdResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ApiResponse.success(new IdResponse(customerService.createCustomer(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        customerService.updateCustomer(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/import")
    public ApiResponse<String> importCustomers(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        CustomerImportListener listener = new CustomerImportListener(customerMapper, userId);
        EasyExcel.read(file.getInputStream(), CustomerImportDto.class, listener).sheet().doRead();
        return ApiResponse.success("导入完成：成功 " + listener.getSuccessCount() + " 条，失败 " + listener.getFailCount() + " 条");
    }

    @GetMapping("/export")
    public void exportCustomers(CustomerQueryRequest request, HttpServletResponse response) throws IOException {
        List<CustomerListItemVo> list = customerService.listAllCustomers(request);

        List<CustomerExportVo> exportList = list.stream().map(vo -> {
            CustomerExportVo exportVo = new CustomerExportVo();
            exportVo.setId(vo.id());
            exportVo.setRealName(vo.realName());
            exportVo.setGrade(vo.grade());
            exportVo.setMajor(vo.major());
            exportVo.setClassName(vo.className());
            exportVo.setProgress(vo.progress());
            exportVo.setIntent(vo.intent());
            exportVo.setOwnerDisplayName(vo.ownerDisplayName());
            exportVo.setInviterDisplayName(vo.inviterDisplayName());
            exportVo.setCreatedAt(vo.createdAt() != null ? vo.createdAt().toString() : "");
            return exportVo;
        }).toList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("客户列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), CustomerExportVo.class).sheet("客户列表").doWrite(exportList);
    }

    @GetMapping("/import-template")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("客户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), CustomerImportDto.class).sheet("客户导入模板").doWrite(List.of());
    }

    @PostMapping("/churn/evaluate")
    public ApiResponse<ChurnEvaluateResponse> evaluateChurn() {
        return ApiResponse.success(churnRiskService.evaluateAll());
    }
}
