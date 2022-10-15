package com.example.secondservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/second-service")
public class TestController {

	@GetMapping("/welcome")
	public String welcome() {
		return "Welcome to the Second service";
	}


	@GetMapping("/message")
	public String message(@RequestHeader("second-request") String header) {
		return "Hello world in second service : " + header;
	}
}