package org.example.yukiacademy.controller;

import org.example.yukiacademy.controller.dto.OrderRequestDto;
import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        Order newOrder = orderService.createOrder(getAuthenticatedUserId(), orderRequestDto.getCourseIds());
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getMyOrders() {
        List<Order> orders = orderService.getUserOrders(getAuthenticatedUserId());
        return ResponseEntity.ok(orders);
    }
}