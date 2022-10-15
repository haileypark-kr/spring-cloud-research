package com.example.firstservice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.discovery.converters.Auto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/first-service")
@RequiredArgsConstructor
public class TestController {

	private final Environment environment;

	@GetMapping("/welcome")
	public String welcome() {
		return "Welcome to the First service";
	}

	@GetMapping("/message")
	public String message(@RequestHeader("first-request") String header) {
		return "Hello world in first service : " + header;
	}

	@GetMapping("/check")
	public String check(HttpServletRequest request) {
		log.info("server port: {}", request.getServerPort());
		log.info("check");
		return String.format("Hi there. this is message from first service %s", environment.getProperty("local.server.port"));
	}
}
