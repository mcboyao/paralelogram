package com.paralelogram.auth.controller;

import com.paralelogram.auth.model.AccessToken;
import com.paralelogram.auth.model.TokenStatus;
import com.paralelogram.auth.model.UserCredentials;
import com.paralelogram.auth.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class TokenControllerTest {

    @InjectMocks
    private TokenController tokenController;

    @Mock
    private AuthService authService;

    @Test
    public void givenUsernameAndPassword_whenGenerateToken_thenReturnAccessToken() {
        AccessToken expected = AccessToken.builder().accessToken(UUID.randomUUID().toString())
                .refreshToken(UUID.randomUUID().toString()).build();
        UserCredentials input = UserCredentials.builder().username("user").password("pass").build();
        Mockito.when(authService.generateUserToken(input)).thenReturn(expected);
        AccessToken actual = tokenController.generateToken(input);

        Mockito.verify(authService, Mockito.times(1)).generateUserToken(input);

        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenRefreshToken_whenRefreshUserToken_thenReturnAccessToken() {
        AccessToken expected = AccessToken.builder().accessToken(UUID.randomUUID().toString())
                .refreshToken(UUID.randomUUID().toString()).build();
        String input = UUID.randomUUID().toString();
        Mockito.when(authService.refreshUserToken(input)).thenReturn(expected);
        AccessToken actual = tokenController.refreshToken(input);

        Mockito.verify(authService, Mockito.times(1)).refreshUserToken(input);

        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenUserIsLoggedIn_whenValidateToken_thenReturnTokenStatusValidIsTrue() {
        TokenStatus expected = new TokenStatus(true);
        Mockito.when(authService.validateLoggedInUserToken()).thenReturn(expected);
        TokenStatus actual = tokenController.validateToken();

        Mockito.verify(authService, Mockito.times(1)).validateLoggedInUserToken();
        Assertions.assertTrue(actual.isValid());
    }

}
