package org.example.yukiacademy.service;

import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.OrderItem;
import org.example.yukiacademy.repository.OrderRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.OrderItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, CourseRepository courseRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order createOrder(Long userId, List<Long> courseIds) {
        logger.info("Creando orden para userId: {}, con {} cursos.", userId, courseIds.size());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con ID: " + courseId));

            int quantity = 1;
            BigDecimal itemPrice = course.getPrice();
            BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setCourse(course);
            orderItem.setQuantity(quantity);
            orderItem.setPriceAtPurchase(itemPrice);
            orderItem.setSubtotal(itemSubtotal);
            orderItems.add(orderItem);
        }
        logger.debug("Monto total calculado para la orden: {}", totalAmount);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus(org.example.yukiacademy.model.Order.OrderStatus.PENDING);

        order = orderRepository.save(order);
        logger.info("Orden inicial guardada con ID: {}", order.getId());

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }
        order.setOrderItems(orderItems);

        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        logger.info("Obteniendo órdenes para el usuario con ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        return orderRepository.findByUser(user);
    }
}