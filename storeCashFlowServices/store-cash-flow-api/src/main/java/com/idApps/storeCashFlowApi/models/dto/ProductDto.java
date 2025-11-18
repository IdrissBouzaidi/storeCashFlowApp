package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    private Integer id;
    private String label;

    @JsonAlias({"image_src", "imageSrc"})
    private String imageSrc;

    @JsonAlias({"state_id", "stateId"})
    private Integer stateId;

    @JsonAlias({"adding_date", "addingDate"})
    private Date addingDate;

    @JsonAlias({"adding_time", "addingTime"})
    private Time addingTime;

    private String details;

    @JsonAlias({"created_by", "createdBy"})
    private Integer createdBy;

    @JsonAlias({"transaction_id", "transactionId"})
    private Integer transactionId;

    private List<Integer> categoryIdList;

}
