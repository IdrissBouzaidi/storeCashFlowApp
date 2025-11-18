package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.persistence.entity.InputEntity;
import com.idApps.storeCashFlowApi.persistence.repository.InputRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Data
public class InputServiceImpl implements InputService {

    private final InputRepository inputRepository;

    @Override
    public Optional<InputEntity> findById(Integer id) {
        return this.inputRepository.findById(id);
    }

    @Override
    public InputEntity save(InputEntity inputEntity) {
        return this.inputRepository.save(inputEntity);
    }

    @Override
    public List<InputEntity> saveAll(List<InputEntity> inputEntityList) {
        return this.inputRepository.saveAll(inputEntityList);
    }
}
