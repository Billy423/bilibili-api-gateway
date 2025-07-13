package org.example.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class VerifyCodeGatewayFilter extends AbstractGatewayFilterFactory<VerifyCodeGatewayFilter.Config> {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public VerifyCodeGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Extract parameters from the request
            String serialNumber = exchange.getRequest().getQueryParams().getFirst("serialNumber");
            String verifyCode = exchange.getRequest().getQueryParams().getFirst("verifyCode");

            // Check if serialNumber exists
            if (serialNumber == null || serialNumber.isEmpty()) {
                return chain.filter(exchange);
            }

            // Fetch the stored verifyCode from Redis
            String redisVerifyCode = redisTemplate.opsForValue().get(serialNumber);

            if (redisVerifyCode == null || !redisVerifyCode.equals(verifyCode)) {
                // If verification fails, respond with a 401 Unauthorized status
                return onError(exchange, "Code is incorrect", HttpStatus.UNAUTHORIZED);
            }

            // If verification succeeds, continue the filter chain
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        // Create a response with the error message and status
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String responseBody = "{\n" +
                "    \"success\": false,\n" +
                "    \"msg\": \"" + errorMessage + "\"\n" +
                "}";
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Add any filter configuration properties if needed in the future
    }
}