package org.example.yukiacademy.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.OrderItem;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.model.Role;
import org.example.yukiacademy.exception.CourseNotFoundException;
import org.example.yukiacademy.exception.UserNotFoundException;
import org.example.yukiacademy.exception.ResourceNotFoundException;
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.OrderRepository;
import org.example.yukiacademy.repository.OrderItemRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.repository.PaymentRepository;
import org.example.yukiacademy.dto.mercadopago.PaymentRequestDto;
import org.example.yukiacademy.dto.mercadopago.PreferenceResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.RoundingMode;


@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.public_key}")
    private String publicKey;

    @Value("${mercadopago.access_token}")
    private String accessToken;

    @Value("${mercadopago.notification_url}")
    private String notificationUrl;

    @Value("${mercadopago.back_url.success}")
    private String backUrlSuccess;

    @Value("${mercadopago.back_url.pending}")
    private String backUrlPending;

    @Value("${mercadopago.back_url.failure}")
    private String backUrlFailure;


    private final PreferenceClient preferenceClient;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;


    private PaymentClient paymentClient;
    private MerchantOrderClient merchantOrderClient;


    public MercadoPagoService(PreferenceClient preferenceClient,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              PaymentRepository paymentRepository) {
        this.preferenceClient = preferenceClient;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            this.paymentClient = new PaymentClient();
            this.merchantOrderClient = new MerchantOrderClient();

            logger.info("Mercado Pago Service inicializado con Access Token.");
            logger.debug("Mercado Pago Notification URL: {}", notificationUrl);
        } catch (Exception e) {
            logger.error("Error al inicializar Mercado Pago Service. Verifique el Access Token y la configuración:", e);
            throw new RuntimeException("Fallo al inicializar Mercado Pago Service.", e);
        }
    }

    @Transactional
    public PreferenceResponseDto createPaymentPreference(Long courseId, Integer quantity, Long userId) {
        logger.info("Iniciando creación de preferencia de pago para userId: {}, courseId: {}, quantity: {}", userId, courseId, quantity);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado con ID: " + courseId));
        logger.debug("Curso encontrado: {}", course.getTitle());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        logger.debug("Usuario encontrado: {}", user.getEmail());

        BigDecimal unitPrice = course.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        logger.debug("Monto total calculado: {}", totalAmount);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus(org.example.yukiacademy.model.Order.OrderStatus.PENDING);
        order.setMpPaymentStatus("pending");
        order.setMpPaymentDetail("Pago iniciado con Mercado Pago");
        order = orderRepository.save(order);
        logger.info("Orden inicial guardada en la base de datos con ID: {}", order.getId());

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setCourse(course);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(unitPrice);
        orderItem.setSubtotal(totalAmount);
        orderItemRepository.save(orderItem);
        order.getOrderItems().add(orderItem);


        org.example.yukiacademy.model.Payment ourPaymentRecord = new org.example.yukiacademy.model.Payment();
        ourPaymentRecord.setOrder(order);
        ourPaymentRecord.setAmount(totalAmount);
        ourPaymentRecord.setStatus(org.example.yukiacademy.model.PaymentStatus.PENDING);
        ourPaymentRecord.setPaymentDate(LocalDateTime.now());
        ourPaymentRecord.setPaymentMethod("Mercado Pago");
        ourPaymentRecord = paymentRepository.save(ourPaymentRecord);
        logger.info("Registro de Payment local creado con ID: {}", ourPaymentRecord.getId());


        List<PreferenceItemRequest> items = new ArrayList<>();
        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(String.valueOf(course.getId()))
                .title(course.getTitle())
                .description(course.getDescription())
                .quantity(quantity)
                .currencyId("CLP")
                .unitPrice(unitPrice)
                .build();
        items.add(itemRequest);
        logger.debug("Item para Mercado Pago creado: {}", course.getTitle());


        com.mercadopago.client.preference.PreferencePayerRequest payer =
                com.mercadopago.client.preference.PreferencePayerRequest.builder()
                        .email(user.getEmail())
                        .name(user.getFirstName() + " " + user.getLastName())
                        .build();
        logger.debug("Payer de preferencia de MP creado: {}", user.getEmail());

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .externalReference(String.valueOf(order.getId()))
                .notificationUrl(notificationUrl)
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(backUrlSuccess)
                        .pending(backUrlPending)
                        .failure(backUrlFailure)
                        .build())
                .binaryMode(true)
                .build();
        logger.debug("PreferenciaRequest construida.");

        try {
            Preference preference = preferenceClient.create(preferenceRequest);
            logger.info("Preferencia de Mercado Pago creada exitosamente. ID: {}, Init Point: {}", preference.getId(), preference.getInitPoint());

            order.setMpPreferenceId(preference.getId());
            orderRepository.save(order);

            return new PreferenceResponseDto(preference.getId(), preference.getInitPoint());
        } catch (MPApiException e) {
            logger.error("Error de API de Mercado Pago al crear preferencia. Status: {}, Content={}",
                    e.getStatusCode(),
                    e.getApiResponse().getContent(), e);
            orderRepository.delete(order);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error al procesar el pago: " + (e.getApiResponse().getContent() != null ? e.getApiResponse().getContent() : e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al crear preferencia de pago: {}", e.getMessage(), e);
            orderRepository.delete(order);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al procesar la solicitud de pago.");
        }
    }

    @Transactional
    public void handleMercadoPagoNotification(String topic, String id) {
        logger.info("Procesando notificación de Mercado Pago: Topic = {}, ID = {}", topic, id);

        try {
            if ("payment".equalsIgnoreCase(topic)) {
                processPaymentNotification(Long.valueOf(id));
            } else if ("merchant_order".equalsIgnoreCase(topic)) {
                MerchantOrder mpMerchantOrder = merchantOrderClient.get(Long.valueOf(id));

                if (mpMerchantOrder == null) {
                    logger.error("No se encontraron detalles para la orden de vendedor con ID: {}", id);
                    return;
                }

                logger.info("Orden de vendedor MP obtenida. ID: {}, Pagos asociados: {}", mpMerchantOrder.getId(), mpMerchantOrder.getPayments().size());

                if (mpMerchantOrder.getPayments() == null || mpMerchantOrder.getPayments().isEmpty()) {
                    logger.warn("Orden de vendedor {} no tiene pagos asociados en la notificación 'merchant_order'. Intentando procesar desde el Payment ID si es posible o esperando notificación 'payment'.", mpMerchantOrder.getId());
                    return;
                }

                for (com.mercadopago.resources.merchantorder.MerchantOrderPayment paymentInOrder : mpMerchantOrder.getPayments()) {
                    processPaymentNotification(paymentInOrder.getId());
                }

            } else {
                logger.warn("Notificación de Mercado Pago ignorada: Topic inesperado '{}'", topic);
            }

        } catch (MPApiException e) {
            logger.error("Error de API de Mercado Pago al procesar notificación (ID: {}). Status: {}, Content={}", id, e.getStatusCode(), e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            logger.error("Error general de Mercado Pago al procesar notificación de ID {}:", id, e);
        } catch (NumberFormatException e) {
            logger.error("Error al parsear ID en la notificación: ID = {}", id, e);
        } catch (Exception e) {
            logger.error("Error inesperado al procesar notificación de Mercado Pago con ID {}:", id, e);
        }
    }

    @Transactional
    public void processPaymentNotification(Long mpPaymentId) throws MPException, MPApiException {
        logger.info("Procesando detalles del pago de Mercado Pago con ID: {}", mpPaymentId);

        Payment mpPayment = paymentClient.get(mpPaymentId);

        if (mpPayment == null) {
            logger.error("No se encontraron detalles para el pago de Mercado Pago con ID: {}", mpPaymentId);
            return;
        }

        logger.info("Detalles de pago de Mercado Pago obtenidos: ID={}, Status={}, External Reference={}",
                mpPayment.getId(), mpPayment.getStatus(), mpPayment.getExternalReference());

        Long orderId = Long.valueOf(mpPayment.getExternalReference());
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            logger.error("Orden no encontrada en la base de datos para externalReference: {}", orderId);
            return;
        }

        Order order = optionalOrder.get();

        Optional<org.example.yukiacademy.model.Payment> optionalOurPayment = paymentRepository.findByOrder(order);
        org.example.yukiacademy.model.Payment ourPayment;

        if (optionalOurPayment.isEmpty()) {
            logger.error("Registro de Payment local no encontrado en la base de datos para Order ID: {}. No se puede actualizar el estado del pago.", order.getId());
            return;
        }
        ourPayment = optionalOurPayment.get();


        org.example.yukiacademy.model.Order.OrderStatus newOrderStatus;
        org.example.yukiacademy.model.PaymentStatus newPaymentStatus;

        switch (mpPayment.getStatus()) { // mpPayment.getStatus() devuelve un String
            case "approved":
            case "authorized":
            case "in_process":
                newOrderStatus = org.example.yukiacademy.model.Order.OrderStatus.COMPLETED;
                newPaymentStatus = org.example.yukiacademy.model.PaymentStatus.COMPLETED;
                break;
            case "pending":
                newOrderStatus = org.example.yukiacademy.model.Order.OrderStatus.PENDING;
                newPaymentStatus = org.example.yukiacademy.model.PaymentStatus.PENDING;
                break;
            case "rejected":
            case "cancelled":
                newOrderStatus = org.example.yukiacademy.model.Order.OrderStatus.CANCELLED;
                newPaymentStatus = org.example.yukiacademy.model.PaymentStatus.FAILED;
                break;
            case "refunded":
                newOrderStatus = org.example.yukiacademy.model.Order.OrderStatus.REFUNDED;
                newPaymentStatus = org.example.yukiacademy.model.PaymentStatus.REFUNDED;
                break;
            default:
                newOrderStatus = org.example.yukiacademy.model.Order.OrderStatus.PROCESSING;
                newPaymentStatus = org.example.yukiacademy.model.PaymentStatus.PENDING;
                logger.warn("Estado de Mercado Pago desconocido o no manejado: {}", mpPayment.getStatus());
                break;
        }

        order.setStatus(newOrderStatus);
        order.setMpPaymentId(String.valueOf(mpPayment.getId()));
        order.setMpPaymentStatus(mpPayment.getStatus());
        order.setMpPaymentDetail(mpPayment.getStatusDetail());
        orderRepository.save(order);
        logger.info("Orden {} actualizada a estado: {}. Estado MP: {}", order.getId(), order.getStatus(), order.getMpPaymentStatus());

        ourPayment.setStatus(newPaymentStatus);
        ourPayment.setTransactionId(String.valueOf(mpPayment.getId()));
        ourPayment.setPaymentDate(mpPayment.getDateApproved() != null ?
                mpPayment.getDateApproved().toLocalDateTime() :
                LocalDateTime.now());
        paymentRepository.save(ourPayment);
        logger.info("Pago local {} actualizado a estado: {}. Transaction ID: {}", ourPayment.getId(), ourPayment.getStatus(), ourPayment.getTransactionId());
    }
}