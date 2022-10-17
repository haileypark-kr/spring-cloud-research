package com.example.userservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.userservice.vo.ResponseOrder;

import lombok.Data;

@Data
public class UserDto {

	private String email;

	private String name;

	private String userId;

	private String pwd;

	private String encryptedPwd;

	private LocalDateTime createdAt;

	private List<ResponseOrder> orders;

}
