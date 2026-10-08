package com.anurag.ai.service;

import com.anurag.ai.model.Task;
import com.anurag.ai.model.User;
import com.anurag.ai.repository.TaskRepository;
import com.anurag.ai.repository.UserRepository;
import com.anurag.ai.web.ApiException;
import com.anurag.ai.web.Dto;
import com.anurag.ai.web.Dto.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;

    public TaskService(TaskRepository tasks, UserRepository users) {
        this.tasks = tasks;
        this.users = users;
    }

    @Transactional
    public TaskView create(TaskCreateRequest req) {
        users.findById(req.userId()).filter(u -> User.USER.equals(u.getRole()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found"));
        Task t = new Task();
        t.setTitle(req.title().trim());
        t.setDescription(req.description() == null ? "" : req.description());
        t.setDueDate(req.dueDate());
        t.setUserId(req.userId());
        return Dto.taskView(tasks.save(t));
    }

    @Transactional(readOnly = true)
    public List<TaskView> mine(User user) {
        return tasks.findByUserIdOrderByCompletedAscIdAsc(user.getId()).stream().map(Dto::taskView).toList();
    }

    @Transactional
    public TaskView update(Long id, TaskUpdateRequest req, User caller) {
        boolean admin = User.ADMIN.equals(caller.getRole());
        // 404 (not 403) for other people's tasks so IDs can't be probed.
        Task t = tasks.findById(id).filter(x -> admin || x.getUserId().equals(caller.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!admin && (req.title() != null || req.description() != null || req.dueDate() != null)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only change the completion status of your tasks");
        }
        if (req.title() != null) t.setTitle(req.title().trim());
        if (req.description() != null) t.setDescription(req.description());
        if (req.dueDate() != null) t.setDueDate(req.dueDate());
        if (req.completed() != null) t.setCompleted(req.completed());
        return Dto.taskView(tasks.save(t));
    }

    @Transactional
    public void delete(Long id) {
        Task t = tasks.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        tasks.delete(t);
    }
}
