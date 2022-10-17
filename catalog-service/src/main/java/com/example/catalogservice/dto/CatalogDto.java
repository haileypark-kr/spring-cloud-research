package com.example.catalogservice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CatalogDto implements Serializable {

	private String productId;
	private String productName;
	private Integer stock;
	private Integer unitPrice;
	private LocalDateTime createdAt;

	private String orderId;
	private String userId;
}
