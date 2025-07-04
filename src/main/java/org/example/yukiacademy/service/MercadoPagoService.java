package org.example.yukiacademy.service;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.OrderItem;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.exception.CourseNotFoundException;
import org.example.yukiacademy.exception.UserNotFoundException;
import org.example.yukiacademy.model.PaymentPreferenceResponse;
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.OrderItemRepository;
import org.example.yukiacademy.repository.OrderRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.model.OrderStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    private final PreferenceClient preferenceClient;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${mercadopago.notification_url}")
    private String notificationUrl;

    @Value("${mercadopago.back_url.success}")
    private String backUrlSuccess;

    @Value("${mercadopago.back_url.pending}")
    private String backUrlPending;

    @Value("${mercadopago.back_url.failure}")
    private String backUrlFailure;


    public MercadoPagoService(PreferenceClient preferenceClient,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository) {
        this.preferenceClient = preferenceClient;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        // --- ¡LOGS CRÍTICOS EN EL CONSTRUCTOR PARA VERIFICAR LA INYECCIÓN AL INICIO! ---
        // Estos logs deben aparecer en la consola de Spring Boot una vez al iniciar la aplicación.
        logger.info("MercadoPagoService constructor: notificationUrl = '{}'", notificationUrl);
        logger.info("MercadoPagoService constructor: backUrlSuccess = '{}'", backUrlSuccess);
        logger.info("MercadoPagoService constructor: backUrlPending = '{}'", backUrlPending);
        logger.info("MercadoPagoService constructor: backUrlFailure = '{}'", backUrlFailure);
        // --- FIN LOGS EN CONSTRUCTOR ---
    }

    @Transactional
    public PaymentPreferenceResponse createPaymentPreference(Long courseId, Integer quantity, Long userId) {
        logger.info("Iniciando creación de preferencia de pago para courseId: {} y userId: {}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado con ID: " + courseId));
        logger.debug("Curso encontrado: {}", course.getTitle());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        logger.debug("Usuario encontrado: {}", user.getEmail());

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(course.getPrice().multiply(new BigDecimal(quantity)));
        order = orderRepository.save(order);

        logger.info("Orden inicial guardada en la base de datos con ID: {}", order.getId());

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setCourse(course);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(course.getPrice());
        order.getOrderItems().add(orderItem);
        orderItemRepository.save(orderItem);

        List<PreferenceItemRequest> items = new ArrayList<>();
        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(String.valueOf(course.getId()))
                .title(course.getTitle())
                .description(course.getDescription())
                .quantity(quantity)
                .currencyId("CLP")
                .unitPrice(course.getPrice().setScale(2, RoundingMode.HALF_UP)) // Asegurar 2 decimales
                .build();
        items.add(itemRequest);
        logger.debug("Item para Mercado Pago creado: {}", course.getTitle());

        // --- ¡LOGS CRÍTICOS EN EL MÉTODO createPaymentPreference! ---
        // Estos logs deben aparecer en la consola de Spring Boot cada vez que se llama a este método.
        logger.debug("backUrlSuccess (en createPaymentPreference): '{}'", backUrlSuccess);
        logger.debug("backUrlPending (en createPaymentPreference): '{}'", backUrlPending);
        logger.debug("backUrlFailure (en createPaymentPreference): '{}'", backUrlFailure);
        // --- FIN LOGS EN MÉTODO ---

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .externalReference(String.valueOf(order.getId()))
                .notificationUrl(notificationUrl)
                .autoReturn("approved")
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(backUrlSuccess)
                        .pending(backUrlPending)
                        .failure(backUrlFailure)
                        .build())
                .build();
        logger.debug("PreferenciaRequest construida.");

        try {
            // ANTES:
            // Preference preference = new PreferenceClient().create(preferenceRequest);
            
            // DESPUÉS:
            Preference preference = preferenceClient.create(preferenceRequest);
            logger.info("Preferencia de Mercado Pago creada exitosamente. ID: {}", preference.getId());

            return new PaymentPreferenceResponse(preference.getId(), preference.getInitPoint());
        } catch (MPApiException e) {
            // Mejorar el logging para incluir más detalles del error
            logger.error("Error de API de Mercado Pago al crear preferencia. Status: {}, Response: {}", 
                e.getStatusCode(), 
                e.getApiResponse().getContent());
            logger.error("Detalles de la solicitud: courseId={}, quantity={}, userId={}", 
                course.getId(), quantity, userId);
            
            orderRepository.delete(order);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Error al procesar el pago: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            logger.error("Error inesperado al crear preferencia de pago: {}", e.getMessage(), e);
            orderRepository.delete(order);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al procesar la solicitud de pago.");
        }
    }
}