package com.example.catalogservice.messagequeue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.catalogservice.jpa.Catalog;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

	private final CatalogRepository repository;

	@KafkaListener(topics = "example-catalog-service")
	public void updateQuantity(String kafkaMessage) {
		log.info("kafka updateQuantity : {}", kafkaMessage);

		Map<Object, Object> map = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();

		try {
			map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Catalog catalogEntity = repository.findByProductId((String)map.get("productId"));
		if (catalogEntity != null) {
			catalogEntity.setStock(catalogEntity.getStock() - (Integer)map.get("quantity"));
			repository.save(catalogEntity);
		}
	}
}
