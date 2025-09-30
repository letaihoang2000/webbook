package com.example.webbook.repository;

import com.example.webbook.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.role r WHERE r.roleName != 'ADMIN'")
    List<User> findUsersExcludingAdmin();

    @Query("SELECT u FROM User u JOIN u.role r WHERE r.roleName != 'ADMIN'")
    Page<User> findUsersExcludingAdmin(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.role r WHERE r.roleName != 'ADMIN' " +
            "AND (LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<User> findUsersExcludingAdminWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(String email);

    // Count users excluding admin (for pagination info)
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.roleName != 'ADMIN'")
    long countUsersExcludingAdmin();

    // Count users with search
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.roleName != 'ADMIN' " +
            "AND (LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    long countUsersExcludingAdminWithSearch(@Param("searchQuery") String searchQuery);
}
