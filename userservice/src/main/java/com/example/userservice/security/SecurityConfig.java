package com.example.userservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final UserService userService;
	private final BCryptPasswordEncoder passwordEncoder;
	private final Environment environment;

	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userService).passwordEncoder(this.passwordEncoder);
	}

	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		http.authorizeRequests()

			.antMatchers("/actuator/**")
			.permitAll()

			.antMatchers("/redispub")
			.permitAll()

			.antMatchers("/**")
			.hasIpAddress(environment.getProperty("gateway.ip"))

			.and()
			.addFilter(this.getAuthenticationFilter());
	}

	private AuthenticationFilter getAuthenticationFilter() throws Exception {
		AuthenticationFilter authenticationFilter = new AuthenticationFilter(this.userService, this.environment);
		authenticationFilter.setAuthenticationManager(this.authenticationManager());
		return authenticationFilter;
	}

}
