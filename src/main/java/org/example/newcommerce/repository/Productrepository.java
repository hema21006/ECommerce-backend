package org.example.newcommerce.repository;

import org.example.newcommerce.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Productrepository extends MongoRepository<Product, String> {
    List<Product> findByCategory(String category);
}
