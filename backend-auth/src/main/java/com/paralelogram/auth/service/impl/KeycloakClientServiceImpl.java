package com.paralelogram.auth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.paralelogram.auth.exception.AuthException;
import com.paralelogram.auth.model.keycloak.KeycloakAccessToken;
import com.paralelogram.auth.service.KeycloakClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class KeycloakClientServiceImpl implements KeycloakClientService {

    private static final String SCOPE_KEY = "scope";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public KeycloakClientServiceImpl(
            @Value("${KEYCLOAK_URI}") String baseUrl,
            @Value("${KEYCLOAK_REALM}") String realm,
            @Value("${KEYCLOAK_CLIENT_ID}") String clientId,
            @Value("${KEYCLOAK_CLIENT_SECRET}") String clientSecret) {
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public KeycloakAccessToken getUserAccessToken(String username, String password) {
        log.info("getting user access token for {}", username);
        return WebClient.create(baseUrl).post()
                .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD)
                        .with(SCOPE_KEY, "openid")
                        .with(CLIENT_ID_KEY, clientId)
                        .with(CLIENT_SECRET_KEY, clientSecret)
                        .with(USERNAME_KEY, username)
                        .with(PASSWORD_KEY, password))
                .retrieve()
                .toEntity(KeycloakAccessToken.class)
                .map(response -> {
                    if (HttpStatus.OK.equals(response.getStatusCode())) {
                        KeycloakAccessToken accessToken = response.getBody();
                        return accessToken;
                    }
                    throw new AuthException(HttpStatus.resolve(response.getStatusCode().value()), "unable to get access token");
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new AuthException(HttpStatus.resolve(error.getStatusCode().value()), "error encountered while getting access token", error))
                .block();
    }

    @Override
    public KeycloakAccessToken refreshToken(String refreshToken) {
        log.info("refreshing access token");
        return WebClient.create(baseUrl).post()
                .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData(GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN)
                        .with(SCOPE_KEY, "openid")
                        .with(CLIENT_ID_KEY, clientId)
                        .with(CLIENT_SECRET_KEY, clientSecret)
                        .with(REFRESH_TOKEN_KEY, refreshToken))
                .retrieve()
                .toEntity(KeycloakAccessToken.class)
                .map(response -> {
                    if (HttpStatus.OK.equals(response.getStatusCode())) {
                        KeycloakAccessToken accessToken = response.getBody();
                        return accessToken;
                    }
                    throw new AuthException(HttpStatus.resolve(response.getStatusCode().value()), "unable to refresh user token");
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new AuthException(HttpStatus.resolve(error.getStatusCode().value()), "error encountered while refreshing token", error))
                .block();
    }

    @Override
    public boolean validateLoggedInUserToken() {
        return WebClient.create(baseUrl).get()
                .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getLoggedInUserBearerToken())
                .retrieve()
                .toEntity(JsonNode.class)
                .map(response -> {
                    if (HttpStatus.OK.equals(response.getStatusCode())) {
                        log.debug("token is valid");
                        return true;
                    }
                    throw new AuthException(HttpStatus.resolve(response.getStatusCode().value()), "invalid token");
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new AuthException(HttpStatus.resolve(error.getStatusCode().value()), "unable to get logged in userinfo", error))
                .block();
    }

    private String getLoggedInUserBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            log.debug("user is authenticated");
            return jwt.getTokenValue();
        }
        throw new AuthException(HttpStatus.UNAUTHORIZED, "user not authenticated");
    }

}
