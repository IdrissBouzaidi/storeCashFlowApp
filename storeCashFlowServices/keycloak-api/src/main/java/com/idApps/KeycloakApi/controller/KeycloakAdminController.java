package com.idApps.KeycloakApi.controller;

import com.idApps.KeycloakApi.dto.UserDto;
import com.idApps.KeycloakApi.dto.request.PasswordRequest;
import com.idApps.KeycloakApi.dto.request.UserRequest;
import com.idApps.KeycloakApi.keycloak.KeyCloakService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class KeycloakAdminController {
    @Autowired
    private KeyCloakService keyCloakService;

    /**
     * Cette méthode ajoute l'utilisateur dans Keycloak.
     * @param user
     * @return
     */
    @SecurityRequirement(name = "keycloak")
    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> addUser(@AuthenticationPrincipal Jwt jwt, @RequestBody UserRequest user) {
        return this.keyCloakService.addUser(jwt.getTokenValue(), user);
    }

    @SecurityRequirement(name = "keycloak")
    @PutMapping("/admin/users/password")
    public ResponseEntity setUserPassword(@AuthenticationPrincipal Jwt jwt, @RequestParam("clientId") String clientId, @RequestBody PasswordRequest data) {
        ResponseEntity response = this.keyCloakService.setUserPassword(jwt.getTokenValue(), clientId, data.getPassword());
        return response;
    }

    @SecurityRequirement(name = "keycloak")
    @DeleteMapping("/admin/users")
    public ResponseEntity<UserDto> deleteUser(@AuthenticationPrincipal Jwt jwt, @RequestParam("id") String userId) {
        return this.keyCloakService.deleteUser(jwt.getTokenValue(), userId);
    }
}
