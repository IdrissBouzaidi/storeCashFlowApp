package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.persistence.entity.InputEntity;

import java.util.List;
import java.util.Optional;

public interface InputService {
    Optional<InputEntity> findById(Integer id);
    InputEntity save(InputEntity inputEntity);

    List<InputEntity> saveAll(List<InputEntity> inputEntityList);
}
