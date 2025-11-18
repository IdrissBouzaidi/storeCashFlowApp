package com.idApps.userApi.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProfilDto {
    private Integer id;

    private String label;

    private String code;
}
