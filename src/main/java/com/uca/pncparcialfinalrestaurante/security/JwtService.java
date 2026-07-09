package com.uca.pncparcialfinalrestaurante.security;

import com.uca.pncparcialfinalrestaurante.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms}")
    private Long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshExpirationMs;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        if (user.getRestaurant() != null) {
            claims.put("restaurantId", user.getRestaurant().getId());
        }

        return buildToken(claims, user.getUsername(), accessExpirationMs);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("userId", user.getId());

        return buildToken(claims, user.getUsername(), refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> claims, String subject, Long expirationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isAccessTokenValid(String token, User user) {
        return isTokenValid(token, user, "access");
    }

    public boolean isRefreshTokenValid(String token, User user) {
        return isTokenValid(token, user, "refresh");
    }

    private boolean isTokenValid(String token, User user, String expectedType) {
        String username = extractUsername(token);
        String tokenType = extractTokenType(token);

        return username.equals(user.getUsername())
                && expectedType.equals(tokenType)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}