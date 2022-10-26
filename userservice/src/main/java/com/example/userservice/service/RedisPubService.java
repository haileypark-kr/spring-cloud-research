package com.example.userservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.ScenarioDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisPubService {
	private final RedisTemplate<String, Object> redisTemplate;

	public void sendMessage(ScenarioDto chatMessage) {
		redisTemplate.convertAndSend("scenario-knowledge", chatMessage);

	}
}
