package com.paralelogram.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paralelogram.auth.exception.AuthException;
import com.paralelogram.auth.model.keycloak.KeycloakAccessToken;
import com.paralelogram.auth.service.impl.KeycloakClientServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import okhttp3.mockwebserver.MockWebServer;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;


public class KeycloakClientServiceTest {

    private KeycloakClientServiceImpl keycloakClientService;

    private MockWebServer mockBackEnd;
    private final ObjectMapper om = new ObjectMapper();
    private final BasicJsonTester json = new BasicJsonTester(this.getClass());

    @BeforeEach
    public void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        keycloakClientService = new KeycloakClientServiceImpl(baseUrl, "paralelogram", "test_client", "test_secret");

        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void givenValidUsernameAndPassword_whenGetUserAccessToken_thenReturnKeycloakAccessToken() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));

        KeycloakAccessToken actual = keycloakClientService.getUserAccessToken("user", "pass");

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        JsonNode body = extractBodyKeyValue(recordedRequest);

        Assertions.assertEquals("POST", recordedRequest.getMethod());
        Assertions.assertEquals("/realms/paralelogram/protocol/openid-connect/token", recordedRequest.getPath());
        Assertions.assertEquals("user", body.get("username").asText());
        Assertions.assertEquals("pass", body.get("password").asText());
        Assertions.assertEquals("password", body.get("grant_type").asText());

        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenInvalidUsernameAndPassword_whenGetUserAccessToken_thenThrowAuthException() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));
        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.getUserAccessToken("user", "pass"));
        Assertions.assertEquals("error encountered while getting access token", exception.getMessage());
    }

    @Test
    public void givenUsernameAndPasswordWith204Response_whenGetUserAccessToken_thenThrowAuthException() throws Exception {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(204)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.getUserAccessToken("user", "pass"));
        Assertions.assertEquals("unable to get access token", exception.getMessage());
    }

    @Test
    public void givenValidRefreshToken_whenRefreshToken_thenReturnKeycloakAccessToken() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));

        String input = UUID.randomUUID().toString();
        KeycloakAccessToken actual = keycloakClientService.refreshToken(input);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        JsonNode body = extractBodyKeyValue(recordedRequest);

        Assertions.assertEquals("POST", recordedRequest.getMethod());
        Assertions.assertEquals("/realms/paralelogram/protocol/openid-connect/token", recordedRequest.getPath());
        Assertions.assertEquals(input, body.get("refresh_token").asText());
        Assertions.assertEquals("refresh_token", body.get("grant_type").asText());

        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenInvalidRefreshToken_whenRefreshToken_thenThrowAuthException() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.refreshToken("invalid"));
        Assertions.assertEquals("error encountered while refreshing token", exception.getMessage());
    }

    @Test
    public void givenRefreshTokenWith204Response_whenRefreshToken_thenThrowAuthException() throws Exception {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(204)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String input = UUID.randomUUID().toString();
        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.refreshToken(input));
        Assertions.assertEquals("unable to refresh user token", exception.getMessage());
    }

    @Test
    public void givenUserIsAuthenticatedAndWithPrincipal_whenValidateLoggedInUserToken_thenReturnTrue() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Jwt jwt = Mockito.mock(Jwt.class);
        Mockito.when(jwt.getTokenValue()).thenReturn("test_token");
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwt);

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(JsonNodeFactory.instance.objectNode())));

        boolean actual = keycloakClientService.validateLoggedInUserToken();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        Assertions.assertEquals("GET", recordedRequest.getMethod());
        Assertions.assertEquals("/realms/paralelogram/protocol/openid-connect/userinfo", recordedRequest.getPath());
        Assertions.assertEquals("Bearer test_token", recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));

        Assertions.assertTrue(actual);
    }

    @Test
    public void givenInvalidUserIsAuthenticatedWithPrincipal_whenValidateLoggedInUserToken_thenThrowAuthException() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Jwt jwt = Mockito.mock(Jwt.class);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwt);

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(JsonNodeFactory.instance.objectNode())));

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.validateLoggedInUserToken());

        Assertions.assertEquals("unable to get logged in userinfo", exception.getMessage());
    }

    @Test
    public void givenUserIsAuthenticatedWithPrincipalWith204Response_whenValidateLoggedInUserToken_thenThrowAuthException() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Jwt jwt = Mockito.mock(Jwt.class);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwt);

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(204)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.validateLoggedInUserToken());

        Assertions.assertEquals("invalid token", exception.getMessage());
    }

    @Test
    public void givenUserIsAuthenticatedWithoutPrincipal_whenValidateLoggedInUserToken_thenThrowAuthException() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(null);

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.validateLoggedInUserToken());
        Assertions.assertEquals("user not authenticated", exception.getMessage());
    }

    @Test
    public void givenUserIsNotAuthenticated_whenValidateLoggedInUserToken_thenThrowAuthException() {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        AuthException exception = Assertions.assertThrows(AuthException.class,
                () -> keycloakClientService.validateLoggedInUserToken());
        Assertions.assertEquals("user not authenticated", exception.getMessage());
    }

    private KeycloakAccessToken getDefaultMockKeycloakAccessToken() {
        return KeycloakAccessToken.builder()
                .accessToken(UUID.randomUUID().toString()).refreshToken(UUID.randomUUID().toString())
                .expiresIn(600L).refreshExpiresIn(1800L).tokenType("bearer").build();
    }

    private JsonNode extractBodyKeyValue(RecordedRequest recordedRequest) {
        JsonContent<Object> body = json.from(recordedRequest.getBody().readUtf8());
        String parameters = body.getJson();
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        Arrays.stream(parameters.split("&"))
                .map(p -> p.split("=")).collect(Collectors.toList())
                .forEach(kv -> node.put(kv[0], kv[1]));
        return node;
    }

}
