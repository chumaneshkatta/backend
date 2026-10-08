package com.anurag.ai.web;

import com.anurag.ai.service.EmployeeService;
import com.anurag.ai.web.Dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequireAdmin
public class EmployeeController {
    private final EmployeeService employees;

    public EmployeeController(EmployeeService employees) { this.employees = employees; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeView create(@Valid @RequestBody EmployeeCreateRequest req) { return employees.create(req); }

    @GetMapping
    public PageResponse<EmployeeView> list(@RequestParam(defaultValue = "") String search,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return employees.list(search, page, size);
    }

    @GetMapping("/{id}")
    public EmployeeView get(@PathVariable Long id) { return employees.get(id); }

    @PutMapping("/{id}")
    public EmployeeView update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest req) {
        return employees.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { employees.delete(id); }
}
