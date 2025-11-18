package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {
    private Integer id;
    private String label;
    private String details;

    @JsonAlias({"image_src", "imageSrc"})
    private String imageSrc;

    @JsonAlias({"adding_date", "addingDate"})
    private Date addingDate;

    @JsonAlias({"adding_time", "addingTime"})
    private Time addingTime;

    @JsonAlias({"created_by", "createdBy"})
    private Integer createdBy;

    @JsonAlias({"state_id", "stateId"})
    private Integer stateId;
}
