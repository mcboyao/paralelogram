package com.paralelogram.auth.service.impl;

import com.paralelogram.auth.model.AccessToken;
import com.paralelogram.auth.model.TokenStatus;
import com.paralelogram.auth.model.UserCredentials;
import com.paralelogram.auth.model.keycloak.KeycloakAccessToken;
import com.paralelogram.auth.service.AuthService;
import com.paralelogram.auth.service.KeycloakClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakClientService keycloakClientService;

    @Override
    public AccessToken generateUserToken(UserCredentials credentials) {
        KeycloakAccessToken keycloakAccessToken = keycloakClientService.getUserAccessToken(credentials.getUsername(), credentials.getPassword());
        return new AccessToken(keycloakAccessToken.getAccessToken(), keycloakAccessToken.getRefreshToken());
    }

    @Override
    public AccessToken refreshUserToken(String refreshToken) {
        KeycloakAccessToken keycloakAccessToken = keycloakClientService.refreshToken(refreshToken);
        return new AccessToken(keycloakAccessToken.getAccessToken(), keycloakAccessToken.getRefreshToken());
    }

    @Override
    public TokenStatus validateLoggedInUserToken() {
        return new TokenStatus(keycloakClientService.validateLoggedInUserToken());
    }
}
