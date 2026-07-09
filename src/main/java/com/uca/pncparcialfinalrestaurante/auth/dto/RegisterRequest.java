package com.uca.pncparcialfinalrestaurante.auth.dto;

import com.uca.pncparcialfinalrestaurante.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Role role;

    /*
     * Solo se usa si el usuario será SHIFT_MANAGER.
     * Para ADMIN o CLIENT puede ir null.
     */
    private Long restaurantId;
}