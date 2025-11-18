package com.idApps.KeycloakApi.controller;

import com.idApps.KeycloakApi.dto.TokenDto;
import com.idApps.KeycloakApi.dto.request.UserSummaryRequest;
import com.idApps.KeycloakApi.keycloak.KeyCloakService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * It's role is to do things before authentication and to get a specific user details from it's tokens after authentication.
 */
@RestController
@RequestMapping("/api/v1")
public class AccountController {

    @Autowired
    private KeyCloakService keyCloakService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody UserSummaryRequest user) throws Exception {
        ResponseEntity<TokenDto> response = this.keyCloakService.login(user);
        return response;
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("/users/id")
    public ResponseEntity<String> getId(@AuthenticationPrincipal Jwt jwt) {
        System.out.println("user id: " + jwt.getSubject());
        return ResponseEntity.ok(jwt.getSubject());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("/users/isTokenValid")
    public ResponseEntity<Boolean> verifyAccessTokenValidity(@AuthenticationPrincipal Jwt jwt) {
        System.out.println("Token value: " + jwt.getTokenValue());
        return this.keyCloakService.verifyAccessTokenValidity(jwt.getTokenValue());
    }
}
