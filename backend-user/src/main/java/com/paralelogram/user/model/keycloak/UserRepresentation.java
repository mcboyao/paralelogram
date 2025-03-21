package com.paralelogram.user.model.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRepresentation {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;
    private Boolean enabled;
    private List<String> realmRoles;
    private List<CredentialRepresentation> credentials;

}
