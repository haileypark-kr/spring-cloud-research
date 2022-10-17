package com.example.orderservice.jpa;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import lombok.Data;

@Data
@Entity
@Table(name = "tbl_orders")
public class Order implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, length = 120, unique = true)
	private String productId;

	@Column(nullable = false, length = 120)
	private String userId;

	@Column(nullable = false, length = 120, unique = true)
	private String orderId;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Integer unitPrice;

	@Column(nullable = false)
	private Integer totalPrice;

	@Column(nullable = false, insertable = false, updatable = false)
	@ColumnDefault(value = "CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

}
