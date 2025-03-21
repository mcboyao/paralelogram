package com.paralelogram.user.model.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CredentialRepresentation {

    private String type;
    private String value;
    private Boolean temporary;

}
