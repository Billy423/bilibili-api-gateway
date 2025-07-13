# bilibili-api-gateway

This repository provides the API Gateway layer for the Bilibili platform. Key features:

- Built with Spring Cloud Gateway and registered as a Eureka Client.
- Routes requests under `/bilibili-api/**` to downstream services.
- Applies a Redis-backed `VerifyCodeGatewayFilter` to validate request tokens.
- FastJSON HTTP message conversion.
