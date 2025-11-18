package com.idApps.KeycloakApi.help.utilClasses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpHeader {
    private String key;
    private String value;
}
