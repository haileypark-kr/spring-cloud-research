package com.example.orderservice.messagequeue;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	// 주문 들어오면 주문 send
	public OrderDto send(String topic, OrderDto orderDto) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = "";
		try {
			jsonInString = mapper.writeValueAsString(orderDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		kafkaTemplate.send(topic, jsonInString);

		log.info("Kafka Producer sent data from order microservice: {}", orderDto);

		return orderDto;
	}
}
