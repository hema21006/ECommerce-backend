package org.example.newcommerce.controller;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.example.newcommerce.model.Order;
import org.example.newcommerce.repository.OrderRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private OrderRepository orderRepository;

    // Step 1: Create Razorpay order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            int amount = (int) body.get("amount");
            JSONObject options = new JSONObject();
            options.put("amount", amount * 100); // paise
            options.put("currency", "INR");
            options.put("receipt", "order_" + System.currentTimeMillis());
            com.razorpay.Order order = client.orders.create(options);
            return ResponseEntity.ok(order.toJson().toMap());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Step 2: Verify payment + save order to MongoDB
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            String orderId   = (String) body.get("razorpay_order_id");
            String paymentId = (String) body.get("razorpay_payment_id");
            String signature = (String) body.get("razorpay_signature");

            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                // Save order to MongoDB
                Order order = new Order();
                order.setUserEmail(authentication.getName());
                order.setPaymentId(paymentId);
                order.setRazorpayOrderId(orderId);
                order.setStatus("CONFIRMED");

                // Get total and items from request body
                Object totalObj = body.get("totalAmount");
                if (totalObj != null) {
                    order.setTotalAmount(Double.parseDouble(totalObj.toString()));
                }

                // Parse items if sent from frontend
                Object itemsObj = body.get("items");
                if (itemsObj instanceof List<?> itemsList) {
                    List<Order.OrderItem> orderItems = itemsList.stream().map(i -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> item = (Map<String, Object>) i;
                        Order.OrderItem oi = new Order.OrderItem();
                        oi.setName((String) item.getOrDefault("name", ""));
                        oi.setImage((String) item.getOrDefault("image", ""));
                        oi.setCategory((String) item.getOrDefault("category", ""));
                        oi.setPrice(Double.parseDouble(item.getOrDefault("price", 0).toString()));
                        oi.setQty(Integer.parseInt(item.getOrDefault("qty", 1).toString()));
                        return oi;
                    }).toList();
                    order.setItems(orderItems);
                }

                orderRepository.save(order);

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "paymentId", paymentId,
                        "message", "Payment verified and order saved!"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("status", "failure", "message", "Invalid signature"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}