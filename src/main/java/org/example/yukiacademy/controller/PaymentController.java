package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.mercadopago.PaymentRequestDto;
import org.example.yukiacademy.dto.mercadopago.PreferenceResponseDto;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.MercadoPagoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return userDetails.getId();
        } else {
            logger.error("El principal de autenticación no es UserDetailsImpl. Tipo: {}", principal.getClass().getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Tipo de usuario no reconocido.");
        }
    }

    @PostMapping("/create-preference")
    public ResponseEntity<PreferenceResponseDto> createPreference(@Valid @RequestBody PaymentRequestDto request) { // Ajustado a PreferenceResponseDto
        logger.info("Solicitud para crear preferencia de pago recibida: CourseId={}, Quantity={}",
                request.getCourseId(), request.getQuantity());

        Long userId = getAuthenticatedUserId();
        logger.debug("ID de usuario autenticado: {}", userId);

        PreferenceResponseDto response = mercadoPagoService.createPaymentPreference(
                request.getCourseId(),
                request.getQuantity(),
                userId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.OK)
    public void handleMercadoPagoNotification(
            @RequestParam("topic") String topic,
            @RequestParam("id") String id) {
        logger.info("Mercado Pago Notification received: Topic = {}, ID = {}", topic, id);
        mercadoPagoService.handleMercadoPagoNotification(topic, id);
    }
}