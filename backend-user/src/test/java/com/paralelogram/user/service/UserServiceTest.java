package com.paralelogram.user.service;

import com.paralelogram.user.entity.Role;
import com.paralelogram.user.entity.User;
import com.paralelogram.user.exception.UserException;
import com.paralelogram.user.model.AddUserRequest;
import com.paralelogram.user.model.RoleRequest;
import com.paralelogram.user.model.keycloak.RoleRepresentation;
import com.paralelogram.user.model.keycloak.UserRepresentation;
import com.paralelogram.user.repository.RoleRepository;
import com.paralelogram.user.repository.UserRepository;
import com.paralelogram.user.service.impl.KeycloakClientServiceImpl;
import com.paralelogram.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private KeycloakClientServiceImpl keycloakClientService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<UserRepresentation> userRepresentationCaptor;

    @Captor
    private ArgumentCaptor<List<RoleRepresentation>> rolesRepresentationCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    public void givenValidUserName_whenGetUser_thenReturnUser() {
        String userName = "user";
        Mockito.when(userRepository.findByUserName(userName)).thenReturn(Optional.of(getDefaultUser(userName)));

        User result = userService.getUser(userName);

        Mockito.verify(userRepository, Mockito.times(1)).findByUserName(userName);
        Assertions.assertEquals(userName, result.getUserName());
    }

    @Test
    public void givenInvalidUserName_whenGetUser_thenThrowUserException() {
        String userName = "invalid";
        Mockito.when(userRepository.findByUserName(userName)).thenReturn(Optional.empty());

        UserException exception = Assertions.assertThrows(UserException.class,
                () -> userService.getUser(userName));

        Mockito.verify(userRepository, Mockito.times(1)).findByUserName(userName);
        Assertions.assertEquals("unable to find user " + userName, exception.getMessage());
    }

    @Test
    public void givenValidUserRequest_whenAddUser_thenReturnNewUser() {
        AddUserRequest input = getDefaultAddUserRequest("user", RoleRequest.ADMIN);

        UUID userId = UUID.randomUUID();
        Role role = Role.builder().roleName(RoleRequest.ADMIN.getValue()).roleId(UUID.randomUUID()).build();

        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any(String.class))).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findByRoleName(ArgumentMatchers.any(String.class)))
                .thenReturn(Optional.of(role));
        Mockito.when(keycloakClientService.createUser(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(userId);
        Mockito.when(keycloakClientService.addUserRole(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(List.class))).thenReturn(true);
        userService.addUser(input);

        Mockito.verify(userRepository, Mockito.times(1)).findByUserName(input.getUserName());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleName(RoleRequest.ADMIN.getValue());
        Mockito.verify(keycloakClientService, Mockito.times(1)).createUser(userRepresentationCaptor.capture());
        Mockito.verify(keycloakClientService, Mockito.times(1)).addUserRole(ArgumentMatchers.any(UUID.class), rolesRepresentationCaptor.capture());
        Mockito.verify(userRepository, Mockito.times(1)).save(userCaptor.capture());

        User captured = userCaptor.getValue();

        Assertions.assertEquals(userId, captured.getUserId());
        Assertions.assertEquals(input.getUserName(), captured.getUserName());
        Assertions.assertEquals(RoleRequest.ADMIN.getValue(), captured.getRole().getRoleName());
    }

    @Test
    public void givenUserWithExistingUserName_whenAddUser_thenThrowUserException() {
        AddUserRequest input = getDefaultAddUserRequest("user", RoleRequest.ADMIN);

        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any(String.class))).thenReturn(Optional.of(User.builder().userName(input.getUserName()).build()));

        UserException exception = Assertions.assertThrows(UserException.class, () -> userService.addUser(input));

        Mockito.verify(userRepository, Mockito.times(1)).findByUserName(input.getUserName());
        Assertions.assertEquals("userName " + input.getUserName() + " already exists", exception.getMessage());
    }

    @Test
    public void givenUserWithInvalidRole_whenAddUser_thenThrowUserException() {
        AddUserRequest input = getDefaultAddUserRequest("user", RoleRequest.VISITOR);
        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any(String.class))).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findByRoleName(input.getRole().getValue())).thenReturn(Optional.empty());

        UserException exception = Assertions.assertThrows(UserException.class, () -> userService.addUser(input));

        Mockito.verify(userRepository, Mockito.times(1)).findByUserName(input.getUserName());
        Assertions.assertEquals("invalid role " + input.getRole().name(), exception.getMessage());
    }

    @Test
    public void givenValidUserRequestAndFailedKeycloakUserCreation_whenAddUser_thenThrowUserException() {
        AddUserRequest input = getDefaultAddUserRequest("user", RoleRequest.ADMIN);

        Role role = Role.builder().roleName(RoleRequest.ADMIN.getValue()).roleId(UUID.randomUUID()).build();

        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any(String.class))).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findByRoleName(ArgumentMatchers.any(String.class)))
                .thenReturn(Optional.of(role));
        Mockito.when(keycloakClientService.createUser(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(null);

        UserException exception = Assertions.assertThrows(UserException.class, () -> userService.addUser(input));

        Mockito.verify(keycloakClientService, Mockito.times(0)).addUserRole(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(List.class));
        Assertions.assertEquals("unable to create user", exception.getMessage());
    }

    @Test
    public void givenValidUserRequestAndFailedKeycloakUserRoleCreation_whenAddUser_thenThrowUserException() {
        AddUserRequest input = getDefaultAddUserRequest("user", RoleRequest.ADMIN);

        UUID userId = UUID.randomUUID();
        Role role = Role.builder().roleName(RoleRequest.ADMIN.getValue()).roleId(UUID.randomUUID()).build();

        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any(String.class))).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findByRoleName(ArgumentMatchers.any(String.class)))
                .thenReturn(Optional.of(role));
        Mockito.when(keycloakClientService.createUser(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(userId);
        Mockito.when(keycloakClientService.addUserRole(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(List.class))).thenReturn(false);
        Mockito.when(keycloakClientService.deleteUser(ArgumentMatchers.any(UUID.class))).thenReturn(true);

        UserException exception = Assertions.assertThrows(UserException.class, () -> userService.addUser(input));

        Mockito.verify(userRepository, Mockito.times(0)).save(ArgumentMatchers.any(User.class));
        Mockito.verify(keycloakClientService, Mockito.times(1)).deleteUser(ArgumentMatchers.any(UUID.class));
        Assertions.assertEquals("unable to create user", exception.getMessage());
    }


    private User getDefaultUser(String userName) {
        return User.builder()
                .userId(UUID.randomUUID())
                .userName(userName)
                .email("email@test.com")
                .build();
    }

    private AddUserRequest getDefaultAddUserRequest(String userName, RoleRequest role) {
        return AddUserRequest.builder()
                .userName(userName)
                .firstName("Sample")
                .lastName("User")
                .email("test@test.com")
                .password("pass")
                .role(role)
                .build();
    }

}
