package com.paralelogram.user.service.impl;

import com.paralelogram.user.entity.Role;
import com.paralelogram.user.entity.User;
import com.paralelogram.user.exception.UserException;
import com.paralelogram.user.model.AddUserRequest;
import com.paralelogram.user.model.keycloak.CredentialRepresentation;
import com.paralelogram.user.model.keycloak.RoleRepresentation;
import com.paralelogram.user.model.keycloak.UserRepresentation;
import com.paralelogram.user.repository.RoleRepository;
import com.paralelogram.user.repository.UserRepository;
import com.paralelogram.user.service.KeycloakClientService;
import com.paralelogram.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final KeycloakClientService keycloakClientService;

    @Override
    public User getUser(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserException(HttpStatus.BAD_REQUEST, "unable to find user " + userName));
    }

    @Override
    public User addUser(AddUserRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new UserException(HttpStatus.BAD_REQUEST, "userName " + request.getUserName() + " already exists");
        }

        Role role = roleRepository.findByRoleName(request.getRole().getValue())
                .orElseThrow(() -> new UserException(HttpStatus.BAD_REQUEST, "invalid role " + request.getRole().name()));

        UUID userId = keycloakClientService.createUser(UserRepresentation.builder()
                .username(request.getUserName())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .credentials(Arrays.asList(CredentialRepresentation.builder()
                        .type("password")
                        .value(request.getPassword())
                        .build()))
                .enabled(Boolean.TRUE)
                .build());

        if (userId != null) {
            boolean roleAdded = keycloakClientService.addUserRole(userId, new ArrayList<>() {{ add(RoleRepresentation.builder()
                    .id(role.getRoleId().toString()).name(role.getRoleName()).build()); }} );
            if (roleAdded) {
                return userRepository.save(User.builder()
                        .userId(userId)
                        .userName(request.getUserName())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .role(role)
                        .build());
            } else {
                keycloakClientService.deleteUser(userId);
            }
        }
        throw new UserException(HttpStatus.BAD_REQUEST, "unable to create user");
    }

}
