package com.idApps.KeycloakApi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long matr;
    private String firstName;
    private String lastName;
    private String userId;
    private String email;
    private String password;
}
