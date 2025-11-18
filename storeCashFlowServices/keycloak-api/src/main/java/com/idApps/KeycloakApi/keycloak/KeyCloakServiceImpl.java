package com.idApps.KeycloakApi.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.KeycloakApi.dto.TokenDto;
import com.idApps.KeycloakApi.dto.request.UserRequest;
import com.idApps.KeycloakApi.dto.request.UserSummaryRequest;
import com.idApps.KeycloakApi.help.constants.HttpConstants;
import com.idApps.KeycloakApi.help.utilClasses.HttpHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;


@Service
public class KeyCloakServiceImpl implements KeyCloakService {
    @Value("${keycloak.realm-url}")
    private String realmUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin.realm-url}")
    private String adminRealmUrl;

    @Value("${keycloak.admin.username}")
    private String adminUserName;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin-cli.id}")
    private String adminCliId;

    private WebClient webClient;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private JwtDecoder jwtDecoder;

    public KeyCloakServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public ResponseEntity login(UserSummaryRequest user) throws Exception {
        String requestUrl = this.realmUrl + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", this.clientId);
        body.add("client_secret", this.clientSecret);
        body.add("username", user.getLogin());
        body.add("password", user.getPassword());
        // body.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenDto> response = restTemplate.postForEntity(requestUrl, request, TokenDto.class);
        System.out.println("response value is: " + response);
        return response;
    }

    /*
    @Override
    public ResponseEntity getUserTokenData(LoginRequest user) throws Exception {
        String requestUrl = this.realmUrl + "/protocol/openid-connect/token";
        HttpHeader header = HttpHeader.builder().key(HttpConstants.CONTENT_TYPE_KEY).value(HttpConstants.FORM_URLENCODED).build();
        String requestBody = "grant_type=password" +
                                "&client_id=" + this.clientId +
                                "&client_secret=" + this.clientSecret +
                                "&username=" + user.getLogin() +
                                "&password=" + user.getPassword();
        ResponseEntity tokenData = this.sendPostOrPutRequest(HttpMethod.POST, requestUrl, new HttpHeader[] {header}, requestBody, TokenDto.class);
        System.out.println("Token data: " + tokenData);

        if(tokenData.getStatusCode() == HttpStatus.OK) {
            String accessToken = ((TokenDto) tokenData.getBody()).getAccessToken();
            Map<String, Object> claims = this.decodeAccessToken(accessToken);
            try {
                List roles = ((List)((Map)((Map)(claims.get("resource_access"))).get("StoreCashFlow")).get("roles"));
                if(!roles.contains("store_cash_flow_user")) {
                    throw new Exception();
                }
                Object userData = tokenData.getBody();
                Map<String, Object> userDataMap = new HashMap<>();
                userDataMap.put("data", userData);
                tokenData = ResponseEntity.ok(userDataMap);
            }
            catch (Exception e) {
                Map responseBody = new HashMap();
                responseBody.put("error", "User has not the role that can allow him to be authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            }

        }

        return tokenData;
    }
    */

    public ResponseEntity<TokenDto> getAdminTokenData() {
        String requestUrl = this.realmUrl + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", this.adminCliId);
        body.add("username", this.adminUserName);
        body.add("password", this.adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<TokenDto> response = this.restTemplate.postForEntity(requestUrl, request, TokenDto.class);
        System.out.println("response: " + response);
        return response;
    }

    @Override
    public ResponseEntity<Boolean> verifyAccessTokenValidity(String accessToken) {
        try {
            Jwt jwt = this.jwtDecoder.decode(accessToken);
            return ResponseEntity.ok(true);
        }
        catch(JwtException e) {
            return ResponseEntity.ok(false);
        }
    }

    @Override
    public ResponseEntity addUser(String tokenValue, UserRequest user) {
        String requestUrl = this.adminRealmUrl + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenValue);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("username", user.getEmail());
        body.put("firstName", user.getFirstName());
        body.put("lastName", user.getLastName());
        body.put("email", user.getEmail());
        body.put("emailVerified", true);
        body.put("enabled", true);
        body.put("credentials", List.of(Map.of(
                "type", "password",
                "value", user.getPassword(),
                "temporary", false
        )));
        System.out.println("Map value is: " + body);
        System.out.println("Headers: " + headers);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Object> response = this.restTemplate.postForEntity(requestUrl, request, Object.class);
        System.out.println("addUserToKeycloak : " + response);
        return response;
    }

    @Override
    public ResponseEntity getUserId(String email) {
        String requestUrl = this.adminRealmUrl + "/users?username=" + email + "&exact=true";
        ResponseEntity<TokenDto> adminTokenData = this.getAdminTokenData();
        String adminAccessToken = adminTokenData.getBody().getAccessToken();
        HttpHeader authorizationHeader = HttpHeader.builder()
                                            .key(HttpConstants.AUTHORIZATION_KEY)
                                            .value("Bearer " + adminAccessToken)
                                            .build();
        HttpHeader contentTypeHeader = HttpHeader.builder()
                                            .key(HttpConstants.CONTENT_TYPE_KEY)
                                            .value(HttpConstants.APPLICATION_JSON)
                                            .build();
        ResponseEntity responseEntity = this.sendGetOrDeleteRequest(com.idApps.KeycloakApi.help.utilEnums.HttpMethod.GET, requestUrl, new HttpHeader[] {authorizationHeader, contentTypeHeader}, List.class);
        try {
            System.out.println("Service: getUserId: response status: " + responseEntity.getStatusCode());
            List<Map<String, Object>> responseData = (List<Map<String, Object>>) responseEntity.getBody();
            if(!responseData.isEmpty()) {
                String userId = (String) responseData.get(0).get("id");
                return ResponseEntity.status(responseEntity.getStatusCode()).body(userId);
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        catch (Exception e) {
            return ResponseEntity.status(responseEntity.getStatusCode()).build();
        }
    }

    @Override
    public ResponseEntity deleteUser(String tokenValue, String userId) {
        String requestUrl = this.adminRealmUrl + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenValue);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<Void>(headers);
        ResponseEntity<Void> responseEntity = this.restTemplate.exchange(requestUrl, org.springframework.http.HttpMethod.DELETE, request, Void.class);
        return responseEntity;
    }

    @Override
    public ResponseEntity setUserPassword(String tokenValue, String userId, String password) {

        String requestUrl = this.adminRealmUrl + "/users/" + userId + "/reset-password";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenValue);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("type", "password");
        body.put("value", password);
        body.put("temporary", false);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Void> httpResponse = this.restTemplate.exchange(requestUrl, org.springframework.http.HttpMethod.PUT, request, Void.class);
        return httpResponse;


//        HttpHeader authorizationHeader = HttpHeader.builder().key(HttpConstants.AUTHORIZATION_KEY).value("Bearer " + adminAccessToken).build();
//        HttpHeader contentTypeHeader = HttpHeader.builder().key(HttpConstants.CONTENT_TYPE_KEY).value(HttpConstants.APPLICATION_JSON).build();
//        String requestBody = String.format(
//                """
//                    {
//                        "type": "password",
//                        "value": "%s",
//                        "temporary": false
//                    }
//                """
//                , password);
//        ResponseEntity responseData = this.sendPostOrPutRequest(HttpMethod.PUT, requestUrl, new HttpHeader[] {authorizationHeader, contentTypeHeader}, requestBody, String.class);
//        return responseData;
    }

    private ResponseEntity sendGetOrDeleteRequest(com.idApps.KeycloakApi.help.utilEnums.HttpMethod httpMethod, String requestUrl, HttpHeader[] headers, Class responseClass) {
        System.out.println("Get response: url" + requestUrl + ", headers: " + Arrays.toString(headers));
        WebClient.RequestHeadersUriSpec<?> responseWithMethod =
                switch (httpMethod) {
                    case GET -> this.webClient.get();
                    case DELETE -> this.webClient.delete();
                    default -> this.webClient.head();
                };
        ResponseEntity responseData = (ResponseEntity) responseWithMethod.uri(requestUrl)
                .headers(
                        httpHeaders ->
                            Stream.of(headers).forEach(
                                header -> httpHeaders.add(header.getKey(), header.getValue())
                            )
                ).exchangeToMono(
                        response -> {
                            System.out.println("On accède à cet emplacement");
                            if(response.statusCode() == HttpStatus.NO_CONTENT) {
                                return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
                            }
                            return response.bodyToMono(responseClass)
                                .map(body -> {
                                    // Log du statut et du corps
                                    System.out.println("code: " + response.statusCode() + ", body: " + body);

                                    // Gestion selon le statut HTTP
                                    if (response.statusCode() == HttpStatus.OK) {
                                        return ResponseEntity.ok(body); // Statut 200
                                    } else {
                                        return ResponseEntity.status(response.statusCode()).body(body); // Autres statuts
                                    }
                                });
                        }
                ).block();
        return responseData;
    }

    private ResponseEntity sendPostOrPutRequest(com.idApps.KeycloakApi.help.utilEnums.HttpMethod httpMethod, String requestUrl, HttpHeader[] headers, String requestBody, Class responseClass) {
            WebClient.RequestBodyUriSpec responseWithMethod =
                    switch (httpMethod) {
                        case com.idApps.KeycloakApi.help.utilEnums.HttpMethod.POST -> this.webClient.post();
                        case com.idApps.KeycloakApi.help.utilEnums.HttpMethod.PUT -> this.webClient.put();
                        default -> null;
                    };
        System.out.println("\n\nOn commence à exécuter la requpete dans l'url : " + requestUrl);
        System.out.println("url: " + requestUrl + ", header: " + Arrays.toString(headers) + ", body: " + requestBody);
        ResponseEntity httpResponse = (ResponseEntity) responseWithMethod
            .uri(requestUrl)
            .headers(
                httpHeader -> Stream.of(headers).forEach(
                    header -> httpHeader.add(header.getKey(), header.getValue())
                )
            )
            .bodyValue(requestBody)
            .exchangeToMono(
                response -> {
                    System.out.println("On accède à cet emplacement");
                    if(response.statusCode() == HttpStatus.CREATED || response.statusCode() == HttpStatus.NO_CONTENT) {
                        return Mono.just(ResponseEntity.status(response.statusCode()).build());
                    }
                    else {
                        return response.bodyToMono(responseClass)
                            .map(body -> {
                                // Log du statut et du corps
                                System.out.println("code: " + response.statusCode() + ", body: " + body);

                                // Gestion selon le statut HTTP
                                if (response.statusCode() == HttpStatus.OK) {
                                    return ResponseEntity.ok(body); // Statut 200
                                } else if (response.statusCode() == HttpStatus.CREATED) {
                                    return ResponseEntity.status(HttpStatus.CREATED).body(body); // Statut 201
                                } else {
                                    return ResponseEntity.status(response.statusCode()).body(body); // Autres statuts
                                }
                            });
                    }
                }
            ).block();
        System.out.println(httpResponse);
        return httpResponse;
    }

    public Map<String, Object> decodeAccessToken(String accessToken) throws Exception {
        String[] parts = accessToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> claims = mapper.readValue(payloadJson, Map.class);
        return claims;
    }
}