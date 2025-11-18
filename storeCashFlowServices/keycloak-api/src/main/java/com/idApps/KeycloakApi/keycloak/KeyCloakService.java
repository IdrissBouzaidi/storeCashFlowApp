package com.idApps.KeycloakApi.keycloak;

import com.idApps.KeycloakApi.dto.request.UserRequest;
import com.idApps.KeycloakApi.dto.request.UserSummaryRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface KeyCloakService {

    ResponseEntity login(UserSummaryRequest user) throws Exception;

    /**
     * If token is valid, it returns the user's email, if not, it returns an empty String.
     * @param accessToken
     * @return
     */
    ResponseEntity<Boolean> verifyAccessTokenValidity(String accessToken);

    ResponseEntity addUser(String tokenValue, UserRequest user);

    ResponseEntity getUserId(String userName);

    ResponseEntity deleteUser(String tokenValue, String iserId);

    ResponseEntity setUserPassword(String tokenValue, String userId, String password);

    Map<String, Object> decodeAccessToken(String accessToken) throws Exception;
}
