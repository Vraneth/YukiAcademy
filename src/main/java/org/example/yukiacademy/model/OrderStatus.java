package org.example.yukiacademy.model;

public enum OrderStatus {
    PENDING,       // Pendiente de pago
    COMPLETED,     // Pago aprobado y orden completada
    CANCELLED,     // Pago rechazado o cancelado
    REFUNDED,      // Pago reembolsado
    PROCESSING     // Procesando el pago o la orden
}