package org.example.newcommerce.controller;

import org.example.newcommerce.model.Order;
import org.example.newcommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    // GET /api/orders/my-orders — get all orders for logged-in user
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        String email = authentication.getName();
        List<Order> orders = orderRepository.findByUserEmailOrderByCreatedAtDesc(email);
        return ResponseEntity.ok(orders);
    }

    // POST /api/orders — save a new order (called internally after payment verify)
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order, Authentication authentication) {
        order.setUserEmail(authentication.getName());
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }
}