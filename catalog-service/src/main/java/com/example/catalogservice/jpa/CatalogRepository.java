package com.example.catalogservice.jpa;

import org.springframework.data.repository.CrudRepository;

public interface CatalogRepository extends CrudRepository<Catalog, Long> {
	Catalog findByProductId(String productId);
}
