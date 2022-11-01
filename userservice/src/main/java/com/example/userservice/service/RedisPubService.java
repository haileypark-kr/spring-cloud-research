package com.example.userservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.ScenarioDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPubService {
	private final RedisTemplate<String, Object> redisTemplate;

	public void sendMessage(ScenarioDto chatMessage) {
		redisTemplate.convertAndSend("scenario-knowledge", chatMessage);
		log.info("User service -> Redis published : {}", chatMessage);

	}
}
