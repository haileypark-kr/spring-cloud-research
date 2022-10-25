package com.example.orderservice.messagequeue;

import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.Field;
import com.example.orderservice.dto.KafkaOrderDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderPayload;
import com.example.orderservice.dto.Schema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {

	List<Field> fields = Arrays.asList(new Field("string", true, "order_id")
		, new Field("string", true, "user_id")
		, new Field("string", true, "product_id")
		, new Field("int32", true, "quantity")
		, new Field("int32", true, "total_price")
		, new Field("int32", true, "unit_price")
	);

	Schema schema = Schema.builder()
		.type("struct")
		.optional(false)
		.name("tbl_orders")
		.fields(fields)
		.build();

	private final KafkaTemplate<String, String> kafkaTemplate;

	// 주문 들어오면 주문 send
	public OrderDto send(String topic, OrderDto orderDto) {

		ModelMapper mm = new ModelMapper();
		mm.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		OrderPayload payload = mm.map(orderDto, OrderPayload.class);

		KafkaOrderDto kafkaOrderDto = KafkaOrderDto.builder().schema(schema).payload(payload).build();

		ObjectMapper om = new ObjectMapper();
		String jsonInString = "";
		try {
			jsonInString = om.writeValueAsString(kafkaOrderDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		kafkaTemplate.send(topic, jsonInString);

		log.info("Kafka Producer sent data from order microservice: {}", kafkaOrderDto);

		return orderDto;
	}
}
