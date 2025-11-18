package com.idApps.storeCashFlowApi.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProfilResponse {
    private Integer id;

    private String label;

    private String code;

    private List<ActionResponse> actions;
}
