package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
