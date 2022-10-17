package com.example.userservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDto {

	private String email;

	private String name;

	private String userId;
	
	private String pwd;

	private String encryptedPwd;

	private LocalDateTime createdAt;

}
