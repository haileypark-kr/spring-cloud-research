package com.example.orderservice.controller;

import static org.modelmapper.convention.MatchingStrategies.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.Order;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	private final Environment env;

	private final KafkaProducer kafkaProducer;
	private final OrderProducer orderProducer;

	@GetMapping("/health_check")
	public String status() {
		return "It's working in Order Service on Port " + env.getProperty("local.server.port");
	}

	@PostMapping("/{userId}/orders")
	public ResponseEntity createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder requestOrder) {

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(STRICT);

		OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
		orderDto.setUserId(userId);

		/* JPA. 주문 저장 ==> JPA로 저장하지 않고 kafka sink connector 통해서 저장할 예정
		OrderDto createdOrderDto = orderService.createOrder(orderDto);
		 */

		/* KAFKA로 ORDERS DB에 데이터 저장 */
		orderDto.setOrderId(UUID.randomUUID().toString());
		orderDto.setTotalPrice(requestOrder.getUnitPrice() * requestOrder.getQuantity());

		// KAFKA. 카프카
		kafkaProducer.send("example-catalog-service", orderDto); // catalog-service에 주문 데이터 보내기
		orderProducer.send("tbl_orders", orderDto); // DB tbl_orders 테이블에 주문 데이터 보내기

		ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);

	}

	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrdersByUserId(@PathVariable("userId") String userId) {

		Iterable<Order> orders = orderService.getOrdersByUserId(userId);
		List<ResponseOrder> result = new ArrayList<>();

		ModelMapper mapper = new ModelMapper();

		orders.forEach(o -> {
			result.add(mapper.map(o, ResponseOrder.class));
		});

		return ResponseEntity.ok(result);
	}
	//
	// @GetMapping("/{userId}/order/{orderId}")
	// public ResponseEntity getOrderByOrderId(@PathVariable("userId") String userId,
	// 	@PathVariable("orderId") String orderId) {
	//
	// 	Iterable<Order> orders = orderService.getOrdersByUserId(userId);
	// 	List<ResponseOrder> result = new ArrayList<>();
	//
	// 	ModelMapper mapper = new ModelMapper();
	//
	// 	orders.forEach(o -> {
	// 		result.add(mapper.map(o, ResponseOrder.class));
	// 	});
	//
	// 	return ResponseEntity.ok(result);
	// }

}
