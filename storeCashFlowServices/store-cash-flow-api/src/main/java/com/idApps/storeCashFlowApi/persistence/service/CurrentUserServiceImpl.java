package com.idApps.storeCashFlowApi.persistence.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private RestTemplate template = new RestTemplate();

    @Value("${user-api.url}")
    private String userApiUrl;
    @Override
    public Integer getUserId(String userAccessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(this.userApiUrl + "/api/v1/user")
                        .queryParam("fields", "id")
                        .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userAccessToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity(url, headers);
        ResponseEntity<Map> userIdResponse =  this.template.exchange(url, HttpMethod.GET, request, Map.class);
        return Integer.valueOf((String) userIdResponse.getBody().get("id"));
    }
}
