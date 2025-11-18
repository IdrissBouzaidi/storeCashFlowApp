package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class RefTableDto {
    private Integer id;
    private Object code;
    private String label;
}
