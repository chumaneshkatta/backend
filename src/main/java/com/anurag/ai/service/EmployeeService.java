package com.anurag.ai.service;

import com.anurag.ai.model.Task;
import com.anurag.ai.model.User;
import com.anurag.ai.repository.TaskRepository;
import com.anurag.ai.repository.UserRepository;
import com.anurag.ai.web.ApiException;
import com.anurag.ai.web.Dto;
import com.anurag.ai.web.Dto.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final UserRepository users;
    private final TaskRepository taskRepo;
    private final PasswordEncoder encoder;
    private final RoadmapService roadmap;
    private final SecureRandom random = new SecureRandom();

    public EmployeeService(UserRepository users, TaskRepository taskRepo, PasswordEncoder encoder, RoadmapService roadmap) {
        this.users = users;
        this.taskRepo = taskRepo;
        this.encoder = encoder;
        this.roadmap = roadmap;
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    User requireEmployee(Long id) {
        return users.findById(id).filter(u -> User.USER.equals(u.getRole()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    @Transactional
    public EmployeeView create(EmployeeCreateRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        String password = req.password();
        String temporary = null;
        if (password == null || password.isBlank()) {
            byte[] bytes = new byte[12];
            random.nextBytes(bytes);
            password = temporary = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } else {
            AuthService.checkPassword(password);
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(User.USER);
        u.setDepartment(blankToNull(req.department()));
        u.setPosition(blankToNull(req.position()));
        u.setStartDate(req.startDate());
        users.save(u);
        List<Task> tasks = taskRepo.saveAll(roadmap.generate(u));
        return Dto.employeeView(u, tasks.size(), 0, tasks, temporary);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeView> list(String search, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<User> result = users.searchEmployees(search == null ? "" : search.trim(),
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by("id")));
        Map<Long, long[]> stats = new HashMap<>();
        if (!result.isEmpty()) {
            for (Object[] row : taskRepo.stats(result.getContent().stream().map(User::getId).toList())) {
                stats.put((Long) row[0], new long[]{((Number) row[1]).longValue(), row[2] == null ? 0 : ((Number) row[2]).longValue()});
            }
        }
        List<EmployeeView> items = result.getContent().stream().map(u -> {
            long[] s = stats.getOrDefault(u.getId(), new long[]{0, 0});
            return Dto.employeeView(u, s[0], s[1], null, null);
        }).toList();
        return new PageResponse<>(items, result.getTotalElements(), result.getNumber(), safeSize);
    }

    @Transactional(readOnly = true)
    public EmployeeView get(Long id) {
        User u = requireEmployee(id);
        List<Task> tasks = taskRepo.findByUserIdOrderByIdAsc(id);
        return Dto.employeeView(u, tasks.size(), tasks.stream().filter(Task::isCompleted).count(), tasks, null);
    }

    @Transactional
    public EmployeeView update(Long id, EmployeeUpdateRequest req) {
        User u = requireEmployee(id);
        if (req.fullName() != null) u.setFullName(req.fullName().trim());
        if (req.department() != null) u.setDepartment(blankToNull(req.department()));
        if (req.position() != null) u.setPosition(blankToNull(req.position()));
        if (req.startDate() != null) u.setStartDate(req.startDate());
        users.save(u);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        User u = requireEmployee(id);
        taskRepo.deleteByUserId(id); // cascade: remove the employee's tasks first
        users.delete(u);
    }
}
