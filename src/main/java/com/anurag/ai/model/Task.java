package com.anurag.ai.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tasks", indexes = @Index(columnList = "user_id"))
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 5000) private String description = "";
    @Column(name = "is_completed", nullable = false) private boolean completed;
    private LocalDate dueDate;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean v) { this.completed = v; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate v) { this.dueDate = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
}
