package com.example.Shop.controller;

import com.example.Shop.model.OrderDemo;
import com.example.Shop.repository.OrderDemoRepository;
import com.example.Shop.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class RestOrderDemoController {
    private final OrderDemoRepository orderDemoRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public RestOrderDemoController(OrderDemoRepository orderDemoRepository, OrderItemRepository orderItemRepository) {
        this.orderDemoRepository = orderDemoRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        List<OrderDemo> orderDemos = orderDemoRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("count", orderDemos.size());
        response.put("orders", orderDemos);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable long id) {
        Optional<OrderDemo> orderDemo = orderDemoRepository.findById(id);
        Map<String, Object> response = new HashMap<>();
        if (orderDemo.isPresent()) {
            response.put("order", orderDemo.get());
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Order Not Found");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderDemo orderDemo) {
        Map<String, Object> response = new HashMap<>();
        if(orderDemo.getOrderItems() == null || orderDemo.getOrderItems().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Order must contain at least one item");
            return ResponseEntity.badRequest().body(response);
        }
        BigDecimal totalPrice = orderDemo.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderDemo.getOrderItems().forEach(item -> orderItemRepository.save(item));
        orderDemo.setTotal(totalPrice);
        OrderDemo orderDemoSaved = orderDemoRepository.save(orderDemo);
        response.put("status", "success");
        response.put("order", orderDemoSaved);
        return ResponseEntity.ok(response);
    }
}
