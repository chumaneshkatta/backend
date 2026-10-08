package com.anurag.ai.web;

import com.anurag.ai.model.Task;
import com.anurag.ai.model.User;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/** Request/response shapes. In update requests, null means "leave unchanged". */
public final class Dto {
    private Dto() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Pattern(regexp = "admin|user") String role,
            @Size(max = 80) String department,
            @Size(max = 120) String position) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String tokenType) {}

    public record UserView(Long id, String fullName, String email, String role,
                           String department, String position, LocalDate startDate) {}

    public record TaskView(Long id, String title, String description, boolean completed,
                           LocalDate dueDate, Long userId) {}

    public record TaskCreateRequest(
            @NotNull Long userId,
            @NotBlank @Size(max = 200) String title,
            @Size(max = 5000) String description,
            LocalDate dueDate) {}

    public record TaskUpdateRequest(
            @Size(min = 1, max = 200) String title,
            @Size(max = 5000) String description,
            Boolean completed,
            LocalDate dueDate) {}

    public record EmployeeCreateRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 255) String email,
            @Size(max = 72) String password, // blank/omitted -> temporary password is generated
            @Size(max = 80) String department,
            @Size(max = 120) String position,
            LocalDate startDate) {}

    public record EmployeeUpdateRequest(
            @Size(min = 1, max = 120) String fullName,
            @Size(max = 80) String department,
            @Size(max = 120) String position,
            LocalDate startDate) {}

    /** tasks is only filled for detail responses; temporaryPassword only right after creation. */
    public record EmployeeView(Long id, String fullName, String email, String department, String position,
                               LocalDate startDate, long totalTasks, long completedTasks,
                               List<TaskView> tasks, String temporaryPassword) {}

    public record PageResponse<T>(List<T> items, long total, int page, int size) {}

    public record ChatRequest(@NotBlank @Size(min = 3, max = 500) String question) {}
    public record ChatSource(String title, String snippet) {}
    public record ChatResponse(String answer, List<ChatSource> sources) {}

    public static UserView userView(User u) {
        return new UserView(u.getId(), u.getFullName(), u.getEmail(), u.getRole(),
                u.getDepartment(), u.getPosition(), u.getStartDate());
    }

    public static TaskView taskView(Task t) {
        return new TaskView(t.getId(), t.getTitle(), t.getDescription(), t.isCompleted(), t.getDueDate(), t.getUserId());
    }

    public static EmployeeView employeeView(User u, long total, long done, List<Task> tasks, String tempPassword) {
        return new EmployeeView(u.getId(), u.getFullName(), u.getEmail(), u.getDepartment(), u.getPosition(),
                u.getStartDate(), total, done, tasks == null ? null : tasks.stream().map(Dto::taskView).toList(), tempPassword);
    }
}
