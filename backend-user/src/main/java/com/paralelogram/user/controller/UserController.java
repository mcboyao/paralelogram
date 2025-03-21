package com.paralelogram.user.controller;

import com.paralelogram.user.entity.User;
import com.paralelogram.user.model.AddUserRequest;
import com.paralelogram.user.model.RoleProperty;
import com.paralelogram.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "User API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping(path = "/{userName}")
    @RolesAllowed({RoleProperty.ADMIN, RoleProperty.VISITOR })
    public User getUser(@PathVariable String userName) {
        log.info("getting user information for userName={}", userName);
        return userService.getUser(userName);
    }

    @PostMapping
    @RolesAllowed({ RoleProperty.ADMIN })
    public User addUser(@RequestBody AddUserRequest user) {
        return userService.addUser(user);
    }

}
