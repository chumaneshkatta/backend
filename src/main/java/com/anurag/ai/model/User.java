package com.anurag.ai.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** An "employee" is a User whose role is "user". */
@Entity
@Table(name = "users")
public class User {
    public static final String ADMIN = "admin";
    public static final String USER = "user";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String fullName;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false, length = 10) private String role = USER;
    @Column(length = 80) private String department;
    @Column(name = "job_position", length = 120) private String position;
    private LocalDate startDate;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { this.department = v; }
    public String getPosition() { return position; }
    public void setPosition(String v) { this.position = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { this.startDate = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Instant getCreatedAt() { return createdAt; }
}
