package com.example.catalogservice.service;

import com.example.catalogservice.jpa.Catalog;

public interface CatalogService {

	Iterable<Catalog> getAllCatalogs();
	
}
