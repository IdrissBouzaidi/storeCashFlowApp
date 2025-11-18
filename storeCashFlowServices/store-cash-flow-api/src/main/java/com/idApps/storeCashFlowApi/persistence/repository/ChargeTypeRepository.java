package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ChargeTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeTypeRepository extends JpaRepository<ChargeTypeEntity, Integer> {

    @Query(value = "SELECT image_src as imageSrc FROM charge WHERE id=:id", nativeQuery = true)
    String getImageSrcById(Integer id);
}
