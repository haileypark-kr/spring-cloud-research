package com.example.orderservice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrderPayload {
	private String productId;
	private Integer quantity;
	private Integer unitPrice;
	private Integer totalPrice;

	private String orderId;
	private String userId;
}
