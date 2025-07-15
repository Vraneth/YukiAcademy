package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.User;
import org.example.yukiacademy.model.Role; // Importa Role
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.purchasedCourses WHERE u.id = :userId")
    Optional<User> findByIdWithPurchasedCourses(@Param("userId") Long userId);

    List<User> findByRoles_Name(Role.RoleName roleName);
}