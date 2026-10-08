package com.anurag.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.anurag.ai.service.RagService;
import com.anurag.ai.web.Dto.ChatResponse;
import org.junit.jupiter.api.Test;

class RagServiceTest {
    private final RagService rag = new RagService();

    @Test void vacationQuestionFindsLeavePolicy() {
        ChatResponse r = rag.answer("How many vacation days do I get?");
        assertTrue(r.answer().contains("24 vacation days"));
        assertEquals("Annual leave", r.sources().get(0).title());
    }

    @Test void securityQuestion() {
        assertEquals("IT security", rag.answer("Do I need two-factor authentication?").sources().get(0).title());
    }

    @Test void payrollQuestion() {
        assertTrue(rag.answer("When is payroll paid?").answer().contains("last working day"));
    }

    @Test void unrelatedQuestionGetsFallback() {
        ChatResponse r = rag.answer("What is the airspeed of a swallow?");
        assertEquals(RagService.NOT_FOUND, r.answer());
        assertTrue(r.sources().isEmpty());
    }
}
