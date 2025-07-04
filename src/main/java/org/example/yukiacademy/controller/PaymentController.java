package org.example.yukiacademy.controller;

// Importaciones necesarias para DTOs y Servicios
import org.example.yukiacademy.dto.mercadopago.PaymentRequestDto;
import org.example.yukiacademy.model.PaymentPreferenceResponse; // CORREGIDO: Usar PaymentPreferenceResponse de tu modelo
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.MercadoPagoService;

// Importaciones de Spring Framework
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Si se necesita para roles
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final MercadoPagoService mercadoPagoService;

    public PaymentController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    // Método auxiliar para obtener el ID del usuario autenticado
    // Se utilizará ahora en createPreference
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        // Asegúrate de que el principal sea UserDetailsImpl, que contiene el ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return userDetails.getId();
        } else {
            // Manejar caso donde el principal no es UserDetailsImpl (ej. token inválido, AnonymousAuthenticationToken)
            logger.error("El principal de autenticación no es UserDetailsImpl. Tipo: {}", principal.getClass().getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Tipo de usuario no reconocido.");
        }
    }

    /**
     * Endpoint para crear una preferencia de pago de Mercado Pago.
     * Solo usuarios autenticados pueden acceder.
     * @param request DTO con el ID del curso y la cantidad.
     * @return ResponseEntity con PaymentPreferenceResponse (ID de preferencia y URL de pago).
     */
    @PostMapping("/create-preference")
    // @PreAuthorize("isAuthenticated()") // Puedes añadir esto si necesitas que solo usuarios logeados accedan
    public ResponseEntity<PaymentPreferenceResponse> createPreference(@Valid @RequestBody PaymentRequestDto request) {
        logger.info("Solicitud para crear preferencia de pago recibida: CourseId={}, Quantity={}",
                request.getCourseId(), request.getQuantity());

        Long userId = getAuthenticatedUserId(); // Obtener el ID del usuario autenticado
        logger.debug("ID de usuario autenticado: {}", userId);

        // Llamar al servicio con los parámetros correctos
        PaymentPreferenceResponse response = mercadoPagoService.createPaymentPreference(
                request.getCourseId(),   // Pasa el courseId
                request.getQuantity(),  // Pasa la cantidad
                userId                  // Pasa el userId autenticado
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para recibir las notificaciones IPN (Instant Payment Notifications) de Mercado Pago.
     * Este endpoint DEBE ser público (no necesita autenticación) ya que Mercado Pago lo llamará.
     * El cuerpo de la solicitud es una query string, no JSON.
     * @param topic Tipo de notificación (ej. "payment").
     * @param id ID del recurso (ej. ID de pago).
     */
    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.OK) // Siempre devolver 200 OK a Mercado Pago
    public void handleMercadoPagoNotification(
            @RequestParam("topic") String topic,
            @RequestParam("id") String id) {
        logger.info("Mercado Pago Notification received: Topic = {}, ID = {}", topic, id);
        // Aquí llamarías al servicio para procesar la notificación
        // mercadoPagoService.handleMercadoPagoNotification(topic, id); // Descomentar cuando implementes el método
    }
}
