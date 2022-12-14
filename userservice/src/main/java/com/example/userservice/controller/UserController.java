package com.example.userservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.ScenarioDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.User;
import com.example.userservice.service.RedisPubService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final Greeting greeting;
	private final UserService userService;

	private final Environment env;

	private final RedisPubService redisPubService;

	@GetMapping("/health_check")
	public String status() {
		return "It's working in User Service on Port " + env.getProperty("local.server.port")
			+ ", server.port=" + env.getProperty("server.port")
			+ ", token.secret=" + env.getProperty("token.secret")
			+ ", token.expiration-time=" + env.getProperty("token.expiration-time")
			+ ", gateway.ip=" + env.getProperty("gateway.ip")
			;
	}

	@GetMapping("/welcome")
	public String welcome() {
		return greeting.getMessage();
	}

	@PostMapping("/users")
	public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserDto userDto = mapper.map(user, UserDto.class);
		userService.createUser(userDto);

		ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseUser); // http status 201??? ?????? ?????????????????? ??????.
	}

	@GetMapping("/users")
	public ResponseEntity<List<ResponseUser>> getUsers() {

		Iterable<User> users = userService.getUserByAll();
		List<ResponseUser> result = new ArrayList<>();

		ModelMapper modelMapper = new ModelMapper();

		users.forEach(u -> {
			result.add(modelMapper.map(u, ResponseUser.class));
		});

		return ResponseEntity.ok(result);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseUser> getUserByUserId(@PathVariable("userId") String userId) {

		UserDto userDto = userService.getUserByUserId(userId);
		ModelMapper modelMapper = new ModelMapper();

		ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);

		return ResponseEntity.ok(responseUser);
	}

	@PostMapping("/redispub")
	public ResponseEntity<ScenarioDto> redisPublishTest(@RequestBody ScenarioDto scenarioDto) {

		scenarioDto.setLearnId(UUID.randomUUID().toString());
		redisPubService.sendMessage(scenarioDto);

		return ResponseEntity.ok(scenarioDto);
	}
}
