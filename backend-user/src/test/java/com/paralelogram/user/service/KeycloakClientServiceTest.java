package com.paralelogram.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paralelogram.user.exception.UserException;
import com.paralelogram.user.model.keycloak.CredentialRepresentation;
import com.paralelogram.user.model.keycloak.KeycloakAccessToken;
import com.paralelogram.user.model.keycloak.RoleRepresentation;
import com.paralelogram.user.model.keycloak.UserRepresentation;
import com.paralelogram.user.service.impl.KeycloakClientServiceImpl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
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
        keycloakClientService = new KeycloakClientServiceImpl(baseUrl, "paralelogram", "test_client", "test_secret", null);

        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void givenValidUserRepresentation_whenCreateUser_thenReturnUserUUID() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        UUID userId = UUID.randomUUID();

        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(201).setHeader(HttpHeaders.LOCATION, "http://localhost:8180/admin/realms/paralelogram/users/" + userId);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);

        UUID actual = keycloakClientService.createUser(getDefaultUserRepresentation());
        Assertions.assertEquals(userId, actual);
    }


    @Test
    public void givenInvalidUserRepresentation_whenCreateUser_thenThrowUserException() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();

        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(401);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);

        UserException exception = Assertions.assertThrows(UserException.class,
                () -> keycloakClientService.createUser(getDefaultUserRepresentation()));
        Assertions.assertEquals("error encountered while creating user", exception.getMessage());
    }

    @Test
    public void givenUserRepresentationAnd204Response_whenCreateUser_thenThrowUserException() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();

        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(204);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);

        UserException exception = Assertions.assertThrows(UserException.class,
                () -> keycloakClientService.createUser(getDefaultUserRepresentation()));
        Assertions.assertEquals("unable to create user", exception.getMessage());
    }

    @Test
    public void givenValidRoleRepresentation_whenAddUserRole_thenReturnTrue() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/role-mappings")) {
                    return new MockResponse().setResponseCode(204);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.addUserRole(UUID.randomUUID(), new ArrayList<>() {{ add(getRoleRepresentation()); }});
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void givenRoleRepresentationWith201Response_whenAddUserRole_thenReturnFalse() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/role-mappings")) {
                    return new MockResponse().setResponseCode(201);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.addUserRole(UUID.randomUUID(), new ArrayList<>() {{ add(getRoleRepresentation()); }});
        Assertions.assertEquals(false, actual);
    }

    @Test
    public void givenInvalidRoleRepresentation_whenAddUserRole_thenThrowUserException() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/role-mappings")) {
                    return new MockResponse().setResponseCode(401);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        UserException exception = Assertions.assertThrows(UserException.class,
                () -> keycloakClientService.addUserRole(UUID.randomUUID(), new ArrayList<>() {{ add(getRoleRepresentation()); }}));

        Assertions.assertEquals("error encountered while adding user role", exception.getMessage());
    }

    @Test
    public void givenValidUserId_whenDeleteUser_thenReturnTrue() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if ("DELETE".equals(request.getMethod()) && request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(204);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.deleteUser(UUID.randomUUID());
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void givenValidUserIdWithValidClientToken_whenDeleteUser_thenReturnTrue() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        keycloakClientService = new KeycloakClientServiceImpl(baseUrl, "paralelogram", "test_client", "test_secret", UUID.randomUUID().toString());

        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/userinfo")) {
                    return new MockResponse().setResponseCode(200);
                }
                if ("DELETE".equals(request.getMethod()) && request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(204);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.deleteUser(UUID.randomUUID());
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void givenValidUserIdWithInvalidClientTokenWith201Response_whenDeleteUser_thenReturnTrue() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        keycloakClientService = new KeycloakClientServiceImpl(baseUrl, "paralelogram", "test_client", "test_secret", UUID.randomUUID().toString());

        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();

        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/userinfo")) {
                    return new MockResponse().setResponseCode(201);
                }
                if ("DELETE".equals(request.getMethod()) && request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(204);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.deleteUser(UUID.randomUUID());
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void givenValidUserIdWithInvalidClientTokenWith401Response_whenDeleteUser_thenReturnTrue() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        keycloakClientService = new KeycloakClientServiceImpl(baseUrl, "paralelogram", "test_client", "test_secret", UUID.randomUUID().toString());

        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();

        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getPath().contains("/userinfo")) {
                    return new MockResponse().setResponseCode(401);
                }
                if ("DELETE".equals(request.getMethod()) && request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(204);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.deleteUser(UUID.randomUUID());
        Assertions.assertEquals(true, actual);
    }

    @Test
    public void givenUserIdWith201Response_whenDeleteUser_thenReturnFalse() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if ("DELETE".equals(request.getMethod()) && request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(201);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        boolean actual = keycloakClientService.deleteUser(UUID.randomUUID());
        Assertions.assertEquals(false, actual);
    }

    @Test
    public void givenInvalidUserId_whenDeleteUser_thenThrowUserException() {
        KeycloakAccessToken accessToken = getDefaultMockKeycloakAccessToken();
        Dispatcher mDispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/token")) {
                    try {
                        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(om.writeValueAsString(accessToken));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if ("DELETE".equals(request.getMethod()) && request.getPath().contains("/users")) {
                    return new MockResponse().setResponseCode(401);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockBackEnd.setDispatcher(mDispatcher);
        UserException exception = Assertions.assertThrows(UserException.class,
                () -> keycloakClientService.deleteUser(UUID.randomUUID()));

        Assertions.assertEquals("error encountered while deleting user", exception.getMessage());
    }

    @Test
    public void givenValidClientIdAndSecret_whenGetAccessToken_thenReturnKeycloakAccessToken() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));

        KeycloakAccessToken actual = keycloakClientService.getAccessToken("clientId", "clientSecret");

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        JsonNode body = extractBodyKeyValue(recordedRequest);

        Assertions.assertEquals("POST", recordedRequest.getMethod());
        Assertions.assertEquals("/realms/paralelogram/protocol/openid-connect/token", recordedRequest.getPath());
        Assertions.assertEquals("clientId", body.get("client_id").asText());
        Assertions.assertEquals("clientSecret", body.get("client_secret").asText());
        Assertions.assertEquals("client_credentials", body.get("grant_type").asText());

        Assertions.assertEquals(expected.getAccessToken(), actual.getAccessToken());
        Assertions.assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    public void givenInvalidClientIdAndSecret_whenGetAccessToken_thenThrowUserException() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));

        UserException exception = Assertions.assertThrows(UserException.class,
                () -> keycloakClientService.getAccessToken("clientId", "clientSecret"));
        Assertions.assertEquals("error encountered while getting client token", exception.getMessage());
    }

    @Test
    public void givenClientIdAndSecretWith204Response_whenGetAccessToken_thenThrowUserException() throws Exception {
        KeycloakAccessToken expected = getDefaultMockKeycloakAccessToken();
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(204)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(om.writeValueAsString(expected)));

        UserException exception = Assertions.assertThrows(UserException.class,
                () -> keycloakClientService.getAccessToken("clientId", "clientSecret"));
        Assertions.assertEquals("unable to get client token", exception.getMessage());
    }


    private KeycloakAccessToken getDefaultMockKeycloakAccessToken() {
        return KeycloakAccessToken.builder()
                .accessToken(UUID.randomUUID().toString()).refreshToken(UUID.randomUUID().toString())
                .expiresIn(600L).refreshExpiresIn(1800L).tokenType("bearer").build();
    }

    private UserRepresentation getDefaultUserRepresentation() {
        return UserRepresentation.builder()
                .username("user")
                .firstName("First")
                .lastName("Last")
                .email("test@test.com")
                .credentials(Arrays.asList(CredentialRepresentation.builder()
                        .type("password")
                        .value("pass")
                        .build()))
                .enabled(Boolean.TRUE)
                .build();
    }

    private RoleRepresentation getRoleRepresentation() {
        return RoleRepresentation.builder()
                .id(UUID.randomUUID().toString()).name("admin").build();
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
