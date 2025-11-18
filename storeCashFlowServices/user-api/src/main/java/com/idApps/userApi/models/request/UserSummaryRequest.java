package com.idApps.userApi.models.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryRequest {
    private String login;
    private String password;
}
