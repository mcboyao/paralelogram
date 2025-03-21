package com.paralelogram.user.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddUserRequest {

    @NotBlank
    private String userName;
    private String firstName;
    private String lastName;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private RoleRequest role;

}
