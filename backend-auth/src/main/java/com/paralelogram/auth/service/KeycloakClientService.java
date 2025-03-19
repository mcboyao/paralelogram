package com.paralelogram.auth.service;

import com.paralelogram.auth.model.keycloak.KeycloakAccessToken;

public interface KeycloakClientService {

    KeycloakAccessToken getUserAccessToken(String username, String password);

    KeycloakAccessToken refreshToken(String refreshToken);

    boolean validateLoggedInUserToken();

}
