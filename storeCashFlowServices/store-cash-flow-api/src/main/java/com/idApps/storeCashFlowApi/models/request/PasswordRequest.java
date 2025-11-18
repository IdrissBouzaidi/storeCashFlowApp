package com.idApps.storeCashFlowApi.models.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PasswordRequest {
    private String password;
}
