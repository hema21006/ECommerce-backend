package org.example.newcommerce.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String userEmail;
    private String paymentId;
    private String razorpayOrderId;

    private List<OrderItem> items;
    private double totalAmount;

    private String status = "CONFIRMED";
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    public static class OrderItem {
        private int productId;
        private String name;
        private String image;
        private String category;
        private double price;
        private int qty;
    }
}