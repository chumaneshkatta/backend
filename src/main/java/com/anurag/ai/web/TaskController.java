package com.anurag.ai.web;

import com.anurag.ai.model.User;
import com.anurag.ai.service.TaskService;
import com.anurag.ai.web.Dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService tasks;

    public TaskController(TaskService tasks) { this.tasks = tasks; }

    @RequireAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskView create(@Valid @RequestBody TaskCreateRequest req) { return tasks.create(req); }

    @GetMapping("/my-tasks")
    public List<TaskView> mine(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) { return tasks.mine(user); }

    @PutMapping("/{id}")
    public TaskView update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest req,
                           @RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        return tasks.update(id, req, user);
    }

    @RequireAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { tasks.delete(id); }
}
