package com.example.userservice.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
	private String email;
	private String userId;
	private String name;

	private List<ResponseOrder> orders;
}
