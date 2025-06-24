package org.example.yukiacademy.exception; // Crea un nuevo paquete para tus excepciones y manejadores

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Esto hace que esta clase sea un manejador global de excepciones
public class GlobalExceptionHandler {

    // Maneja excepciones de validación de DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        // Puedes personalizar el formato de la respuesta si lo necesitas para tu frontend
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // Maneja la excepción IllegalArgumentException (ej. email ya registrado en AuthService)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // Manejador genérico para cualquier otra excepción no capturada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", "Ha ocurrido un error interno en el servidor.");
        errorDetails.put("details", ex.getMessage()); // Solo para depuración, evita exponer detalles sensibles en producción
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }
}