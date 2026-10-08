package com.anurag.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.anurag.ai.model.Task;
import com.anurag.ai.model.User;
import com.anurag.ai.service.RoadmapService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoadmapServiceTest {
    private final RoadmapService roadmap = new RoadmapService();

    @Test void commonTasksForUnknownDepartment() {
        User u = new User();
        u.setDepartment("Legal");
        assertEquals(5, roadmap.generate(u).size());
    }

    @Test void engineeringGetsExtraTasksSortedByDueDate() {
        User u = new User();
        u.setDepartment(" Engineering ");
        u.setStartDate(LocalDate.of(2030, 1, 1));
        List<Task> tasks = roadmap.generate(u);
        assertEquals(7, tasks.size());
        assertEquals(LocalDate.of(2030, 1, 2), tasks.get(0).getDueDate());
        for (int i = 1; i < tasks.size(); i++) {
            assertFalse(tasks.get(i).getDueDate().isBefore(tasks.get(i - 1).getDueDate()));
        }
    }
}
