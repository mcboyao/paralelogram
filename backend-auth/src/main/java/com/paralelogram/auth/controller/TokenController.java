package com.paralelogram.auth.controller;

import com.paralelogram.auth.model.AccessToken;
import com.paralelogram.auth.model.TokenStatus;
import com.paralelogram.auth.model.UserCredentials;
import com.paralelogram.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token API", description = "")
@RestController
@RequestMapping(path = "/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthService authService;

    @PostMapping(path = "/generate")
    public AccessToken generateToken(@RequestBody UserCredentials credentials) {
        return authService.generateUserToken(credentials);
    }

    @PostMapping(path = "/refresh")
    public AccessToken refreshToken(final String refreshToken) {
        return authService.refreshUserToken(refreshToken);
    }

    @GetMapping(path = "/validate")
    public TokenStatus validateToken() {
        return authService.validateLoggedInUserToken();
    }
}
