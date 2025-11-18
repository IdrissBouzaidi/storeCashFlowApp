package com.idApps.storeCashFlowApi.models.response;

import com.idApps.storeCashFlowApi.models.dto.ActionDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionResponse {
    private Integer id;

    private String label;

    private String code;

    public static ActionResponse instanciateFromActionDto(ActionDto actionDto) {
        return ActionResponse.builder()
                .id(actionDto.getId())
                .label(actionDto.getLabel())
                .code(actionDto.getCode())
                .build();
    }
}
