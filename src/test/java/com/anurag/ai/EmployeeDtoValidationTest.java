package com.anurag.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anurag.ai.web.Dto.EmployeeCreateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmployeeDtoValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createRequestAllowsBlankPasswordForGeneratedTemporaryPassword() {
        EmployeeCreateRequest req = new EmployeeCreateRequest(
                "Ada Example",
                "ada@example.com",
                "",
                "Engineering",
                "Developer",
                LocalDate.now());

        assertTrue(validator.validate(req).isEmpty());
    }
}
