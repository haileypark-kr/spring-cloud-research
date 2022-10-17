package com.example.catalogservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalogservice.jpa.Catalog;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.ResponseCatalog;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/catalog-service")
@RequiredArgsConstructor
public class CatalogController {

	private final CatalogService catalogService;

	@GetMapping("/catalogs")
	public ResponseEntity getAllCatalogs() {
		Iterable<Catalog> catalogs = catalogService.getAllCatalogs();

		List<ResponseCatalog> responseCatalogs = new ArrayList<>();
		ModelMapper mapper = new ModelMapper();
		catalogs.forEach(c -> {
			responseCatalogs.add(mapper.map(c, ResponseCatalog.class));
		});

		return ResponseEntity.ok(responseCatalogs);
	}

}
