package org.example.yukiacademy.service;

import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.OrderItem;
import org.example.yukiacademy.model.OrderStatus;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.OrderItemRepository;
import org.example.yukiacademy.repository.OrderRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserRepository userRepository, CourseRepository courseRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Order createOrder(Long userId, List<Long> courseIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + userId));

        List<Course> courses = courseRepository.findAllById(courseIds);
        if (courses.size() != courseIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más cursos no fueron encontrados.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();

        for (Course course : courses) {
            OrderItem item = new OrderItem();
            item.setCourse(course);
            item.setPriceAtPurchase(course.getPrice());
            item.setOrder(null); // Se establecerá al guardar la orden
            orderItems.add(item);
            totalAmount = totalAmount.add(course.getPrice());
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(orderItems);

        // Guardar la orden primero para que tenga un ID
        Order savedOrder = orderRepository.save(order);

        // Ahora establecer la orden en los OrderItems y guardarlos
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        savedOrder.setOrderItems(orderItems); // Asegurarse de que la colección en la orden esté actualizada
        return savedOrder;
    }

    // Método para actualizar el estado de una orden (ej. después de un pago exitoso)
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con ID: " + orderId));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + userId));
        return orderRepository.findByUser(user);
    }
}