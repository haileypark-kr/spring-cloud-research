package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import feign.Logger;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	@LoadBalanced // IP 가 아니라 microservice 이름으로 호출하려면 이거 붙여줘야 함.
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public Logger.Level feignLoggerLevel() { // feign client 호출 시 로그 확인을 위해
		return Logger.Level.FULL;
	}
}
