package com.example.springcloudgateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 라우팅 정보 각각 적용 가능한 filter
 */
@Slf4j
@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

	public CustomFilter() {
		super(Config.class);
	}

	public static class Config {
		// configuration 정보를 넣어줌.
	}

	@Override
	public GatewayFilter apply(Config config) {
		// pre-filter 적용
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest(); // 기본 was가 netty이며 webflux 기본 지원.
			ServerHttpResponse response = exchange.getResponse();

			log.info("Custom pre filter : request id: " + request.getId());

			// post-filter 적용
			return chain.filter(exchange).then(Mono.fromRunnable( () -> {
				log.info("Custom post filter : response code : " + response.getStatusCode());
			} ));
		};
	}


}
