package com.idApps.userApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String id;
    private String username;
    @JsonAlias({"first_name", "firstName"})
    private String firstName;
    @JsonAlias({"last_name", "lastName"})
    private String lastName;
    private String phone;
}
