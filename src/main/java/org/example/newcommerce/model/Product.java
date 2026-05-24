package org.example.newcommerce.model;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private double price;
    private String image;
    private String category;
    private int stock;

    public Product() {}
    public Product(String name, double price, String image, String category, int stock) {
        this.name = name; this.price = price; this.image = image;
        this.category = category; this.stock = stock;
    }

}
