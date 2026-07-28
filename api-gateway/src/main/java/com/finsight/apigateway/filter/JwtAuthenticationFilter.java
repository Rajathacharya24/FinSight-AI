package com.finsight.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global JWT authentication filter for Spring Cloud Gateway.
 *
 * <p>Enforced behaviour:
 * <ul>
 *   <li>Preflight CORS OPTIONS requests pass through without token validation.</li>
 *   <li>/api/v1/auth/** is whitelisted — no token required.</li>
 *   <li>All other routes require a valid {@code Authorization: Bearer <token>} header.</li>
 *   <li>On success, {@code X-User-Id} and {@code X-User-Roles} headers are forwarded downstream.</li>
 *   <li>On failure, 401 JSON is returned immediately — downstream service never sees the request.</li>
 * </ul>
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /** Paths that do NOT require authentication. */
    private static final List<String> OPEN_PATHS = List.of(
            "/api/v1/auth/",
            "/actuator"
    );

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Override
    public int getOrder() {
        // Run before routing filters
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Always pass OPTIONS through (CORS preflight)
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // 2. Skip auth-required check for open paths
        String path = request.getURI().getPath();
        if (isOpenPath(path)) {
            return chain.filter(exchange);
        }

        // 3. Extract Bearer token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JwtFilter] Missing or malformed Authorization header for {}", path);
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);

        // 4. Validate and parse
        try {
            Claims claims = parseToken(token);
            String userId = claims.getSubject();
            Object roles = claims.get("roles");

            // 5. Forward identity headers downstream
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id",    userId != null ? userId : "")
                    .header("X-User-Roles", roles  != null ? roles.toString() : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtFilter] Token validation failed for {}: {}", path, e.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    // ─────────────── Helpers ───────────────

    private boolean isOpenPath(String path) {
        return OPEN_PATHS.stream().anyMatch(path::startsWith);
    }

    private Claims parseToken(String token) {
        SecretKey key = resolveKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey resolveKey() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is not configured in api-gateway");
        }
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // HMAC-SHA256 needs ≥ 256 bits; pad with env-variable bytes if needed
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
