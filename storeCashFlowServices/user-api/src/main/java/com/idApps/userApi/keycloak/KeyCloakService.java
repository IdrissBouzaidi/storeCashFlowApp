package com.idApps.userApi.keycloak;

import com.idApps.userApi.models.request.UserRequest;
import com.idApps.userApi.models.request.UserSummaryRequest;
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
