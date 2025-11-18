package com.idApps.userApi.models.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ActionDto {
    private Integer id;

    private String label;

    private String code;

    private Integer idProfil;

    public static ActionDto instanciateFromMap(Map<String, Object> map) {
        return ActionDto.builder()
                .id((Integer) map.get("id"))
                .label((String) map.get("label"))
                .code((String) map.get("code"))
                .idProfil((Integer) map.get("id_profil"))
                .build();
    }
}
