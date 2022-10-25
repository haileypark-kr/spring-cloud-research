package com.example.orderservice.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KafkaOrderDto implements Serializable {
	private Schema schema;
	private OrderPayload payload;
}
