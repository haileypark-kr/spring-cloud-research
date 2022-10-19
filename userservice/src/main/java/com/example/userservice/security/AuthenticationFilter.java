package com.example.userservice.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
	private final UserService userService;
	private final Environment environment;

	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {

		try {
			RequestLogin credentials = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

			return this.getAuthenticationManager()
				.authenticate(
					new UsernamePasswordAuthenticationToken(
						credentials.getEmail(),
						credentials.getPassword(),
						new ArrayList<>())
				);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) throws IOException, ServletException {

		String username = ((User)authResult.getPrincipal()).getUsername();
		log.info("[debug] logged in username: {}", username);

		UserDto userDetails = this.userService.getUserByEmail(username);
		String token = Jwts.builder()
			.setSubject(userDetails.getUserId())
			.setExpiration(new Date(
				System.currentTimeMillis() + Long.parseLong(this.environment.getProperty("token.expiration-time"))))
			.signWith(SignatureAlgorithm.HS512, this.environment.getProperty("token.secret"))
			.compact();

		response.addHeader("token", token);
		response.addHeader("userId", userDetails.getUserId());
	}

}
