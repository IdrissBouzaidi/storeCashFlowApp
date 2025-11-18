package com.idApps.storeCashFlowApi.models.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}
