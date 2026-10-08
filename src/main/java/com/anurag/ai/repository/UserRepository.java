package com.anurag.ai.repository;

import com.anurag.ai.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(String role);

    @Query("""
        select u from User u
        where u.role = 'user' and (
            lower(u.fullName) like lower(concat('%', :q, '%'))
            or lower(u.email) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.department, '')) like lower(concat('%', :q, '%')))
        """)
    Page<User> searchEmployees(@Param("q") String q, Pageable pageable);
}
