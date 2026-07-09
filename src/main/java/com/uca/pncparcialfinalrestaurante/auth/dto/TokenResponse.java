package com.uca.pncparcialfinalrestaurante.auth.dto;

import com.uca.pncparcialfinalrestaurante.common.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String username;
    private Role role;
}
