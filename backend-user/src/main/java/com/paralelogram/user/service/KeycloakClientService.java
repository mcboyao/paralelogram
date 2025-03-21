package com.paralelogram.user.service;

import com.paralelogram.user.model.keycloak.KeycloakAccessToken;
import com.paralelogram.user.model.keycloak.RoleRepresentation;
import com.paralelogram.user.model.keycloak.UserRepresentation;

import java.util.List;
import java.util.UUID;

public interface KeycloakClientService {

    UUID createUser(UserRepresentation user);

    boolean addUserRole(UUID userId, List<RoleRepresentation> role);

    boolean deleteUser(UUID userId);

    KeycloakAccessToken getAccessToken(String clientId, String clientSecret);
}
