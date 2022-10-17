package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.Order;

public interface OrderService {
	OrderDto createOrder(OrderDto orderDto);

	OrderDto getOrderByOrderId(String orderId);

	Iterable<Order> getOrdersByUserId(String userId);
}
