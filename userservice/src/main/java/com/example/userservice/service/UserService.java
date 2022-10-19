package com.example.userservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.User;

public interface UserService extends UserDetailsService {

	UserDto createUser(UserDto userDto);

	UserDto getUserByUserId(String userId);

	UserDto getUserByEmail(String email);

	Iterable<User> getUserByAll();
}
