package com.paralelogram.auth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserCredentials {

    private String username;
    private String password;

}
