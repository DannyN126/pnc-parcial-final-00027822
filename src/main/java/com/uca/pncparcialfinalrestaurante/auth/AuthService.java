package com.uca.pncparcialfinalrestaurante.auth;

import com.uca.pncparcialfinalrestaurante.auth.dto.LoginRequest;
import com.uca.pncparcialfinalrestaurante.auth.dto.RefreshTokenRequest;
import com.uca.pncparcialfinalrestaurante.auth.dto.RegisterRequest;
import com.uca.pncparcialfinalrestaurante.auth.dto.TokenResponse;
import com.uca.pncparcialfinalrestaurante.common.enums.Role;
import com.uca.pncparcialfinalrestaurante.restaurant.Restaurant;
import com.uca.pncparcialfinalrestaurante.restaurant.RestaurantRepository;
import com.uca.pncparcialfinalrestaurante.security.JwtService;
import com.uca.pncparcialfinalrestaurante.user.User;
import com.uca.pncparcialfinalrestaurante.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshExpirationMs;

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        Restaurant restaurant = null;

        if (request.getRole() == Role.SHIFT_MANAGER) {
            if (request.getRestaurantId() == null) {
                throw new IllegalArgumentException("El encargado de turno debe tener una sucursal asignada");
            }

            restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .restaurant(restaurant)
                .build();

        userRepository.save(user);

        return generateTokens(user);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return generateTokens(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token no registrado"));

        if (Boolean.TRUE.equals(savedToken.getRevoked())) {
            throw new IllegalArgumentException("Refresh token revocado");
        }

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expirado");
        }

        User user = savedToken.getUser();

        if (!jwtService.isRefreshTokenValid(request.getRefreshToken(), user)) {
            throw new IllegalArgumentException("Refresh token inválido");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    private TokenResponse generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken savedRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(savedRefreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}