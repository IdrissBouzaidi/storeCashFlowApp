package com.idApps.storeCashFlowApi.models.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;

    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
    }
    public ApiResponse(Exception error) {
        this.success = false;
        this.error = error.getMessage();
    }
}
