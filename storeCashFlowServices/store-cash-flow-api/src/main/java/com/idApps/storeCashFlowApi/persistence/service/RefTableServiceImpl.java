package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.models.dto.RefTableDto;
import com.idApps.storeCashFlowApi.persistence.repository.*;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Service
public class RefTableServiceImpl implements RefTableService {
    final private TransactionTypeRepository transactionTypeRepository;

    final private FinancialPeriodRepository financialPeriodRepository;

    final private PeriodStateRepository periodStateRepository;

    final private UserRepository userRepository;

    final private CustomerRepository customerRepository;

    final private ProductTransactionStateRepository productTransactionStateRepository;

    final private ChargeTransactionStateRepository chargeTransactionStateRepository;

    final private CapitalContributionRepository capitalContributionRepository;

    final private CapitalContributionStateRepository capitalContributionStateRepository;

    final private ProductRepository productRepository;

    final private ProductStateRepository productStateRepository;

    final private CategoryRepository categoryRepository;

    final private CategoryStateRepository categoryStateRepository;

    final private ChargeTypeRepository chargeTypeRepository;

    final private ReusableNotConsInputRepository reusableNotConsInputRepository;

    final private NotConsumableInputStateRepository notConsumableInputStateRepository;

    final private AdvanceStateRepository advanceStateRepository;

    final private OutOfPocketStateRepository outOfPocketStateRepository;

    final private CustomerCreditStateRepository customerCreditStateRepository;

    final private ExternalLoanStateRepository externalLoanStateRepository;

    final private ObjectMapper objectMapper;

    @Override
    public List<RefTableDto> getTransactionTypesRefTable() {
        return this.transactionTypeRepository.getTransactionTypesRefTable().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getFinancialPeriodsRefTable() {
        return this.financialPeriodRepository.getFinancialPeriodsRefTable().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getUsersRefTable() {
        return this.userRepository.getUsersRefTable().stream().map(map -> {
            Map<String, Object> resultMap = new HashMap<>();
            String label = map.get("firstName") + " " + map.get("lastName");
            resultMap.put("id", map.get("id"));
            resultMap.put("label", label);
            return resultMap;
        }).map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getProductsTransactionStatesRefTable() {
        return this.productTransactionStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getChargesTransactionStatesRefTable() {
        return this.chargeTransactionStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getCapitalContributionTransactionStatesRefTable() {
        return this.capitalContributionStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getChargeTypesRefTable() {
        return this.chargeTypeRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getProductsRefTable() {
        return this.productRepository.getProductsRefTable().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<RefTableDto> getProductStatesRefTable() {
        return this.productStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getCategoriesRefTable() {
        return this.categoryRepository.getCategoriesRefTable().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<RefTableDto> getCategoryStatesRefTable() {
        return this.categoryStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getReusableInputsRefTable() {
        return this.reusableNotConsInputRepository.getReusableInputsRefTbale().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getNotConsInputStatesRefTable() {
        return this.notConsumableInputStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getAdvanceStatesRefTable() {
        return this.advanceStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getOutOfPocketStatesRefTable() {
        return this.outOfPocketStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getCustomersRefTable() {
        return this.customerRepository.getCustomersRefTable()
                .stream().map(item -> RefTableDto.builder()
                        .id((Integer) item.get("id"))
                        .label(item.get("first_name")  + " " + (String) item.get("last_name"))
                        .build()
                ).toList();
    }

    @Override
    public List<RefTableDto> getCustomerCreditStatesRefTable() {
        return this.customerCreditStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getExternalLoanStatesRefTable() {
        return this.externalLoanStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }

    @Override
    public List<RefTableDto> getPeriodStatesRefTable() {
        return this.periodStateRepository.findAll().stream().map(item -> this.objectMapper.convertValue(item, RefTableDto.class)).toList();
    }
}
