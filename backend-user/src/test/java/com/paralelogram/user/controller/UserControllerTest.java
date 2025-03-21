package com.paralelogram.user.controller;

import com.paralelogram.user.entity.User;
import com.paralelogram.user.model.AddUserRequest;
import com.paralelogram.user.model.RoleRequest;
import com.paralelogram.user.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    public void givenUserName_whenGetUser_thenReturnUser() {
        String userName = "user";
        Mockito.when(userService.getUser(userName)).thenReturn(getDefaultUser(userName));

        User actual = userController.getUser(userName);

        Mockito.verify(userService, Mockito.times(1)).getUser(userName);
        Assertions.assertEquals(userName, actual.getUserName());

    }

    @Test
    public void givenUserRequest_whenAddUser_thenReturnUser() {
        String userName = "user";
        AddUserRequest input = getDefaultAddUserRequest(userName, RoleRequest.ADMIN);
        Mockito.when(userService.addUser(input)).thenReturn(getDefaultUser(userName));

        User actual = userController.addUser(input);

        Mockito.verify(userService, Mockito.times(1)).addUser(input);
        Assertions.assertEquals(userName, actual.getUserName());
    }

    private User getDefaultUser(String userName) {
        return User.builder()
                .userId(UUID.randomUUID())
                .userName(userName)
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
