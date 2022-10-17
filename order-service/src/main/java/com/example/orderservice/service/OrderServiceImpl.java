package com.example.orderservice.service;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.Order;
import com.example.orderservice.jpa.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository repository;

	@Override
	public OrderDto createOrder(OrderDto orderDto) {

		orderDto.setOrderId(UUID.randomUUID().toString());
		orderDto.setTotalPrice(orderDto.getQuantity() * orderDto.getUnitPrice());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		Order orderEntity = mapper.map(orderDto, Order.class);

		repository.save(orderEntity);

		return mapper.map(orderEntity, OrderDto.class);
	}

	@Override
	public OrderDto getOrderByOrderId(String orderId) {

		Order orderEntity = repository.findByOrderId(orderId);

		return new ModelMapper().map(orderEntity, OrderDto.class);
	}

	@Override
	public Iterable<Order> getOrdersByUserId(String userId) {
		return repository.findByUserId(userId);
	}
}
