package com.idApps.userApi.help.utilClasses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpHeader {
    private String key;
    private String value;
}
