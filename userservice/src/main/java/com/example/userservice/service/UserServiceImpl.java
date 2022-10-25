package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.User;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository repository;
	private final BCryptPasswordEncoder passwordEncoder;

	private final RestTemplate restTemplate;
	private final Environment env;

	public UserDto createUser(UserDto userDto) {

		userDto.setUserId(UUID.randomUUID().toString());

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		User userEntity = modelMapper.map(userDto, User.class);
		userEntity.setEncryptedPwd(this.passwordEncoder.encode(userDto.getPwd()));
		this.repository.save(userEntity);

		return modelMapper.map(userEntity, UserDto.class);
	}

	public UserDto getUserByUserId(String userId) {

		User userEntity = this.repository.findByUserId(userId);

		if (userEntity == null) {
			throw new UsernameNotFoundException("user not found");
		} else {

			ModelMapper modelMapper = new ModelMapper();
			modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
			UserDto userDto = modelMapper.map(userEntity, UserDto.class);

			List<ResponseOrder> orders = new ArrayList();

			// order service에서 usreId 로 주문 목록 가져오기
			// 1. RestTemplate 사용.
			String orderUrl = String.format(env.getProperty("order-service.url"), userId);
			ResponseEntity<List<ResponseOrder>> responseEntity = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<ResponseOrder>>() {
					// Generic 사용하려면 ParameterizedTypeReference써야 함.
				});

			orders = responseEntity.getBody();
			userDto.setOrders(orders);

			return userDto;
		}
	}

	public Iterable<User> getUserByAll() {
		return this.repository.findAll();
	}

	public UserDto getUserByEmail(String email) {

		User userEntity = this.repository.findByEmail(email);

		if (userEntity == null) {
			throw new UsernameNotFoundException("user not found");
		} else {
			ModelMapper modelMapper = new ModelMapper();
			modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

			return modelMapper.map(userEntity, UserDto.class);
		}
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userEntity = this.repository.findByEmail(username);
		if (userEntity == null) {
			throw new UsernameNotFoundException(username);
		} else {
			return new org.springframework.security.core.userdetails.User(
				userEntity.getEmail(),
				userEntity.getEncryptedPwd(),
				true, true, true, true,
				new ArrayList());
		}
	}

}
