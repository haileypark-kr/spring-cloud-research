package com.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.example.orderservice.dto.ScenarioDto;
import com.example.orderservice.service.RedisSubService;

@Configuration
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory();
	}

	@Bean
	public RedisTemplate<String, ScenarioDto> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, ScenarioDto> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	// // 리스너어댑터 설정
	@Bean
	MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter(new RedisSubService());
	}

	// 컨테이너 설정
	@Bean
	RedisMessageListenerContainer redisContainer() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory());
		container.addMessageListener(messageListenerAdapter(), topic());
		return container;
	}

	// pub/sub 토픽 설정
	@Bean
	ChannelTopic topic() {
		return new ChannelTopic("scenario-knowledge");
	}
}
