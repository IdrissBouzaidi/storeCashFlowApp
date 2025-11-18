package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.CustomerCreditDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.CustomerCreditEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface CustomerCreditService {

    List<CustomerCreditDto> getCustomerCredits(Date creditDateMin, Date creditDateMax, Integer stateId, Integer periodId, Integer customerId);

    ResponseEntity<ApiResponse<CustomerCreditEntity>> addCustomerCredit(CustomerCreditDto customerCreditDto, String tokenValue);

    ResponseEntity<ApiResponse<CustomerCreditEntity>> cancelCustomerCredit(String tokenValue, int id);
}
