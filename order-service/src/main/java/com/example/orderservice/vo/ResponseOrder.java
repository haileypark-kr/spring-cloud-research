package com.example.orderservice.vo;

import lombok.Data;

@Data
public class ResponseOrder {
	private String productId;
	private Integer quantity;
	private Integer unitPrice;
	private Integer totalPrice;

	private String orderId;
}
