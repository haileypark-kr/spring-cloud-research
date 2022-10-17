package com.example.userservice.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.userservice.entity.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

	UserEntity findByUserId(String userId);

}
