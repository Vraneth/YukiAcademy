package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity // Indica que esta clase es una entidad JPA y se mapea a una tabla de DB
@Table(name = "roles") // Nombre de la tabla en la base de datos
@Data // Anotación de Lombok para generar getters, setters, toString, equals, hashCode
@NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
public class Role {

    @Id // Marca el campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Estrategia para generar valores de ID (auto-incremento)
    private Long id;

    // Define un tipo de enumeración para los roles para asegurar consistencia
    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como un String en la DB
    @Column(length = 20, nullable = false, unique = true) // Limita la longitud y asegura que no sea nulo y sea único
    private RoleName name;

    // Enum anidado para definir los nombres de los roles
    public enum RoleName {
        ROLE_STUDENT,   // Rol para estudiantes
        ROLE_PROFESSOR, // Rol para profesores
        ROLE_ADMIN      // Rol para administradores
    }

    // *** INICIO DEL NUEVO CONSTRUCTOR PARA SOLUCIONAR EL ERROR ***
    // Constructor para inicializar el rol solo con el nombre (útil para la inicialización en DataLoader)
    public Role(RoleName name) {
        this.name = name;
    }
    // *** FIN DEL NUEVO CONSTRUCTOR ***
}