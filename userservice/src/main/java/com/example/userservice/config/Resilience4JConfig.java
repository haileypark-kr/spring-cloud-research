package com.example.userservice.config;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class Resilience4JConfig {

	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
			.failureRateThreshold(4) // 서킷브레이커 오픈을 결정하는 failure rate threshold percentage
			.waitDurationInOpenState(Duration.ofMillis(1000)) // 서킷브레이커 오픈 상태를 유지하는 지속 시간
			.slidingWindowType(
				CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 카운트 기반: 마지막 N번의 호출 결과 집계, 시간 기반: 마지막 N초 동안의 호출 결과 집계
			.slidingWindowSize(2)
			.build();
		TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
			.timeoutDuration(Duration.ofSeconds(4)) // 호출 타임아웃 설정 가능
			.build();

		return factory -> factory.configureDefault(
			id -> new Resilience4JConfigBuilder(id)
				.timeLimiterConfig(timeLimiterConfig)
				.circuitBreakerConfig(circuitBreakerConfig)
				.build()
		);
	}
}
