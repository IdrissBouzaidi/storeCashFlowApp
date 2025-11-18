package com.idApps.userApi.controller;

import com.idApps.userApi.models.dto.ActionDto;
import com.idApps.userApi.models.dto.UserDto;
import com.idApps.userApi.models.response.ProfilResponse;
import com.idApps.userApi.persistence.entity.ProfilEntity;
import com.idApps.userApi.persistence.service.ActionService;
import com.idApps.userApi.persistence.service.ProfilService;
import com.idApps.userApi.persistence.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProfilService profilService;

    @Autowired
    private ActionService actionService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<UserDto> getUserDetails(@AuthenticationPrincipal Jwt jwt,
                                                  @Parameter(
                                                        array = @ArraySchema(
                                                            schema = @Schema(type = "string", allowableValues = {"id", "username", "phone", "firstName", "lastName"})
                                                        )
                                                  )
                                                  @RequestParam(value = "fields", required = false) List<String> fields) throws Exception {
        return ResponseEntity.ok(this.userService.getUserDetails(jwt.getClaim("preferred_username"), fields));
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("/profils")
    public ResponseEntity<List<ProfilEntity>> getUserProfils(@AuthenticationPrincipal Jwt jwt) throws Exception {
        return ResponseEntity.ok(this.profilService.getUserProfils(jwt.getClaim("preferred_username")));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("/profils/actions")
    public ResponseEntity<List<ActionDto>> getProfilesActions(@AuthenticationPrincipal Jwt jwt, @RequestBody List<Integer> profilsIds) {
        return ResponseEntity.ok(this.actionService.getProfilesActions(profilsIds));
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("/actions")
    public ResponseEntity<List<ProfilResponse>> getUserActions(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(this.profilService.getUserActions(jwt.getClaim("preferred_username")));
    }
}
