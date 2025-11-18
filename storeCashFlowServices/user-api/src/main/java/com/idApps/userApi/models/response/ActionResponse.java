package com.idApps.userApi.models.response;

import com.idApps.userApi.models.dto.ActionDto;
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
