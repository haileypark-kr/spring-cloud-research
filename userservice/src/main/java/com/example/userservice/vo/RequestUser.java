package com.example.userservice.vo;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class RequestUser {

	@NotNull(message = "email cannot be null")
	@Size(min = 2, message = "email cannot be less than two characters")
	@Email
	private String email;

	@NotNull(message = "name cannot be null")
	@Size(min = 2, message = "name cannot be less than two characters")
	private String name;

	@NotNull(message = "password cannot be null")
	@Size(min = 8, message = "password cannot be less than eight characters")
	private String pwd;

}
