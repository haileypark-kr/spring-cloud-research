package com.example.catalogservice.service;

import org.springframework.stereotype.Service;

import com.example.catalogservice.jpa.Catalog;
import com.example.catalogservice.jpa.CatalogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

	private final CatalogRepository repository;

	@Override
	public Iterable<Catalog> getAllCatalogs() {
		return repository.findAll();
	}
}
