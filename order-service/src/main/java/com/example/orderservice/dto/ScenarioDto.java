package com.example.orderservice.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ScenarioDto implements Serializable {
	private String learnId; //UUID
	private String intentName;
	private String nodeName;
}
