package com.paralelogram.user.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.paralelogram.user.exception.UserException;
import com.paralelogram.user.model.keycloak.KeycloakAccessToken;
import com.paralelogram.user.model.keycloak.RoleRepresentation;
import com.paralelogram.user.model.keycloak.UserRepresentation;
import com.paralelogram.user.service.KeycloakClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class KeycloakClientServiceImpl implements KeycloakClientService {

    private static final String SCOPE_KEY = "scope";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    private String clientCredentialsToken;

    public KeycloakClientServiceImpl(
            @Value("${KEYCLOAK_URI}") String baseUrl,
            @Value("${KEYCLOAK_REALM}") String realm,
            @Value("${KEYCLOAK_CLIENT_ID}") String clientId,
            @Value("${KEYCLOAK_CLIENT_SECRET}") String clientSecret,
            String clientCredentialsToken) {
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientCredentialsToken = clientCredentialsToken;
    }

    @Override
    public UUID createUser(UserRepresentation user) {
        log.info("creating keycloak user={}", user);
        return WebClient.create(baseUrl).post()
                .uri(baseUrl + "/admin/realms/" + realm + "/users")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getClientCredentialsToken())
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    if (HttpStatus.CREATED.equals(response.getStatusCode())) {
                        String keycloakUserId = StringUtils.substringAfterLast(response.getHeaders().get("Location").get(0), "/");
                        return UUID.fromString(keycloakUserId);
                    }
                    throw new UserException(HttpStatus.resolve(response.getStatusCode().value()), "unable to create user");
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new UserException(HttpStatus.resolve(error.getStatusCode().value()), "error encountered while creating user", error))
                .block();
    }

    @Override
    public boolean addUserRole(UUID userId, List<RoleRepresentation> role) {
        log.info("adding user={} role={}", userId, role);
        return WebClient.create(baseUrl).post()
                .uri(baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getClientCredentialsToken())
                .body(BodyInserters.fromValue(role))
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    if (HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
                        return true;
                    }
                    return false;
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new UserException(HttpStatus.resolve(error.getStatusCode().value()), "error encountered while adding user role", error))
                .block();
    }

    @Override
    public boolean deleteUser(UUID userId) {
        log.info("deleting user={}", userId);
        return WebClient.create(baseUrl).delete()
                .uri(baseUrl + "/admin/realms/" + realm + "/users/" + userId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getClientCredentialsToken())
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    if (HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
                        return true;
                    }
                    return false;
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new UserException(HttpStatus.resolve(error.getStatusCode().value()), "error encountered while deleting user", error))
                .block();
    }

    @Override
    public KeycloakAccessToken getAccessToken(String clientId, String clientSecret) {
        log.info("getting client credentials access token for {}", clientId);
        return WebClient.create(baseUrl).post()
                .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData(GRANT_TYPE_KEY, GRANT_TYPE_CLIENT_CREDENTIALS)
                        .with(CLIENT_ID_KEY, clientId)
                        .with(CLIENT_SECRET_KEY, clientSecret))
                .retrieve()
                .toEntity(KeycloakAccessToken.class)
                .map(response -> {
                    if (HttpStatus.OK.equals(response.getStatusCode())) {
                        KeycloakAccessToken accessToken = response.getBody();
                        return accessToken;
                    }
                    throw new UserException(HttpStatus.resolve(response.getStatusCode().value()), "unable to get client token");
                })
                .onErrorMap(WebClientResponseException.class, error ->
                        new UserException(HttpStatus.resolve(error.getStatusCode().value()), "error encountered while getting client token", error))
                .block();
    }

    private String getClientCredentialsToken() {
        try {
            if (!isTokenValid(clientCredentialsToken)) {
                clientCredentialsToken = getAccessToken(clientId, clientSecret).getAccessToken();
            }
        } catch (UserException ue) {
            clientCredentialsToken = getAccessToken(clientId, clientSecret).getAccessToken();
        }
        return clientCredentialsToken;
    }

    private boolean isTokenValid(String bearerToken) {
        if (StringUtils.isNotBlank(bearerToken)) {
            return WebClient.create(baseUrl).get()
                    .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .toEntity(JsonNode.class)
                    .map(response -> {
                        if (HttpStatus.OK.equals(response.getStatusCode())) {
                            log.debug("token is valid");
                            return true;
                        }
                        throw new UserException(HttpStatus.resolve(response.getStatusCode().value()), "invalid token");
                    })
                    .onErrorMap(WebClientResponseException.class, error ->
                            new UserException(HttpStatus.resolve(error.getStatusCode().value()), "unable to get client userinfo", error))
                    .block();
        }
        return false;
    }

}
