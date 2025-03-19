package com.paralelogram.auth.service;

import com.paralelogram.auth.exception.AuthException;
import com.paralelogram.auth.model.AccessToken;
import com.paralelogram.auth.model.TokenStatus;
import com.paralelogram.auth.model.UserCredentials;
import com.paralelogram.auth.model.keycloak.KeycloakAccessToken;
import com.paralelogram.auth.service.impl.AuthServiceImpl;
import com.paralelogram.auth.service.impl.KeycloakClientServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@ExtendWith({MockitoExtension.class})
public class AuthServiceTest {

    @Mock
    private KeycloakClientServiceImpl keycloakClientService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void givenValidUserCredentials_whenGenerateUserToken_thenReturnAccessToken() {
        UserCredentials input = UserCredentials.builder().username("user").password("pass").build();
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        Mockito.when(keycloakClientService.getUserAccessToken(input.getUsername(), input.getPassword())).thenReturn(expected);
        AccessToken actual = authService.generateUserToken(input);

        Mockito.verify(keycloakClientService, Mockito.times(1)).getUserAccessToken(input.getUsername(), input.getPassword());
        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenInvalidUserCredentials_whenGenerateUserToken_thenThrowAuthException() {
        UserCredentials input = UserCredentials.builder().username("").password("").build();
        Mockito.when(keycloakClientService.getUserAccessToken(input.getUsername(), input.getPassword()))
                .thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "error"));

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> authService.generateUserToken(input));

        Mockito.verify(keycloakClientService, Mockito.times(1)).getUserAccessToken(input.getUsername(), input.getPassword());
        Assertions.assertEquals("error", exception.getMessage());
    }

    @Test
    public void givenValidRefreshToken_whenRefreshUserToken_thenReturnAccessToken() {
        String input = UUID.randomUUID().toString();
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        Mockito.when(keycloakClientService.refreshToken(input)).thenReturn(expected);
        AccessToken actual = authService.refreshUserToken(input);

        Mockito.verify(keycloakClientService, Mockito.times(1)).refreshToken(input);
        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenInvalidRefreshToken_thenRefreshUserToken_thenThrowAuthException() {
        String input = "invalid";
        Mockito.when(keycloakClientService.refreshToken(input)).thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "error"));

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> authService.refreshUserToken(input));

        Mockito.verify(keycloakClientService, Mockito.times(1)).refreshToken(input);
        Assertions.assertEquals("error", exception.getMessage());
    }

    @Test
    public void givenUserIsLoggedIn_whenValidateLoggedInUserToken_thenReturnValidTokenStatus() {
        Mockito.when(keycloakClientService.validateLoggedInUserToken()).thenReturn(true);

        TokenStatus actual = authService.validateLoggedInUserToken();
        Mockito.verify(keycloakClientService, Mockito.times(1)).validateLoggedInUserToken();
        Assertions.assertTrue(actual.isValid());
    }

    @Test
    public void givenUserIsNotLoggedIn_whenValidateLoggedInUserToken_thenThrowAuthException() {
        Mockito.when(keycloakClientService.validateLoggedInUserToken()).thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "error"));
        AuthException exception = Assertions.assertThrows(AuthException.class, () -> authService.validateLoggedInUserToken());

        Mockito.verify(keycloakClientService, Mockito.times(1)).validateLoggedInUserToken();
        Assertions.assertEquals("error", exception.getMessage());
    }

    private KeycloakAccessToken getDefaultMockKeycloakAccessToken() {
        return KeycloakAccessToken.builder()
                .accessToken(UUID.randomUUID().toString()).refreshToken(UUID.randomUUID().toString())
                .expiresIn(600L).refreshExpiresIn(1800L).tokenType("bearer").build();
    }

}
