package com.finsight.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global request/response logging filter.
 *
 * <p>Logs: method, path, response status, latency, and traceId for every request.
 * Intentionally does NOT log headers or body to avoid leaking PII or JWT secrets.
 *
 * <p>Format: [METHOD] /path → STATUS in Xms [traceId=...]
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path   = request.getURI().getRawPath();
        long   startMs = System.currentTimeMillis();

        return chain.filter(exchange).doFinally(signalType -> {
            ServerHttpResponse response = exchange.getResponse();
            int status = response.getStatusCode() != null
                    ? response.getStatusCode().value()
                    : 0;
            long durationMs = System.currentTimeMillis() - startMs;

            // Extract traceId from MDC if propagated by Micrometer
            String traceId = exchange.getRequest().getHeaders().getFirst("traceparent");
            if (traceId == null) traceId = "-";

            if (status >= 500) {
                log.error("[{}] {} → {} in {}ms [traceparent={}]", method, path, status, durationMs, traceId);
            } else if (status >= 400) {
                log.warn("[{}] {} → {} in {}ms [traceparent={}]", method, path, status, durationMs, traceId);
            } else {
                log.info("[{}] {} → {} in {}ms", method, path, status, durationMs);
            }
        });
    }

    /** Run after JwtAuthenticationFilter (order 1) but before downstream forwarding. */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
