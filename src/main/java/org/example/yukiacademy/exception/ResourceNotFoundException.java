// src/main/java/org/example/yukiacademy/exception/ResourceNotFoundException.java
package org.example.yukiacademy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Devuelve un 404 NOT FOUND por defecto
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}