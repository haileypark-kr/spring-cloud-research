package com.example.orderservice.service;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.ScenarioDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisSubService implements MessageListener {

	@Override
	public void onMessage(Message message, byte[] bytes) {

		try {
			ObjectMapper mapper = new ObjectMapper();

			ScenarioDto chatMessage = mapper.readValue(message.getBody(), ScenarioDto.class);

			log.info("Order service -> Redis subscribed : {}", chatMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
