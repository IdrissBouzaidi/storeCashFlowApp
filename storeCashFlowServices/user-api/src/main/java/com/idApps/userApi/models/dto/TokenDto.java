package com.idApps.userApi.models.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonSetter;

@Data
public class TokenDto {
    private String access_token;
    private int expires_in;
    private int refresh_expires_in;
    private String refresh_token;
    private String token_type;
    private int not_before_policy;
    private String session_state;
    private String scope;

    // Getters annotés pour la sérialisation
    @JsonGetter("access_token")
    public String getAccessToken() {
        return access_token;
    }

    @JsonGetter("expires_in")
    public int getExpiresIn() {
        return expires_in;
    }

    @JsonGetter("refresh_expires_in")
    public int getRefreshExpiresIn() {
        return refresh_expires_in;
    }

    @JsonGetter("refresh_token")
    public String getRefreshToken() {
        return refresh_token;
    }

    @JsonGetter("token_type")
    public String getTokenType() {
        return token_type;
    }

    @JsonGetter("not_before_policy")
    public int getNotBeforePolicy() {
        return not_before_policy;
    }

    @JsonGetter("session_state")
    public String getSessionState() {
        return session_state;
    }

    @JsonGetter("scope")
    public String getScope() {
        return scope;
    }

    // Setters annotés pour la désérialisation
    @JsonSetter("access_token")
    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    @JsonSetter("expires_in")
    public void setExpiresIn(int expiresIn) {
        this.expires_in = expiresIn;
    }

    @JsonSetter("refresh_expires_in")
    public void setRefreshExpiresIn(int refreshExpiresIn) {
        this.refresh_expires_in = refreshExpiresIn;
    }

    @JsonSetter("refresh_token")
    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }

    @JsonSetter("token_type")
    public void setTokenType(String tokenType) {
        this.token_type = tokenType;
    }

    @JsonSetter("not_before_policy")
    public void setNotBeforePolicy(int notBeforePolicy) {
        this.not_before_policy = notBeforePolicy;
    }

    @JsonSetter("session_state")
    public void setSessionState(String sessionState) {
        this.session_state = sessionState;
    }

    @JsonSetter("scope")
    public void setScope(String scope) {
        this.scope = scope;
    }
}