package com.example.springcloudgateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

	private Environment environment;

	public AuthorizationHeaderFilter(Environment env) {
		super(Config.class);
		environment = env;
	}

	public static class Config {

	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
			}

			String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			String jwt = authorizationHeader.replace("Bearer ", "");

			if (!isJwtValid(jwt)) {
				return onError(exchange, "JWT Not Valid", HttpStatus.UNAUTHORIZED);

			}

			// post-filter 없음
			return chain.filter(exchange);
		};
	}

	private boolean isJwtValid(String jwt) {

		boolean isValid = true;

		// subject: userId
		String subject = null;
		try {
			subject = Jwts.parser().setSigningKey(environment.getProperty("token.secret"))
				.parseClaimsJws(jwt).getBody().getSubject();
		} catch (Exception e) {
			isValid = false;
		}

		if (subject == null || subject.isEmpty()) {
			isValid = false;
		}

		return isValid;

	}

	// Mono, Flux => Spring Webflux 에서 사용되는 단위값. Mono: 단일 값, Flux: 여러 값
	private Mono<Void> onError(ServerWebExchange exchange, String errorMsg, HttpStatus status) {

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		log.error(errorMsg);

		return response.setComplete(); // Mono 타입으로 response를 내보낼 수 있음.
	}
}
