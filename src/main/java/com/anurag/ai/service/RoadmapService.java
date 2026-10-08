package com.anurag.ai.service;

import com.anurag.ai.model.Task;
import com.anurag.ai.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Automated onboarding roadmap: common tasks plus department-specific ones. */
@Service
public class RoadmapService {
    private record Spec(String title, String description, int dueInDays) {}

    private static final List<Spec> COMMON = List.of(
            new Spec("Complete HR paperwork", "Sign your contract and fill in tax and emergency contact forms.", 1),
            new Spec("Set up your laptop and accounts", "Install required software and enable two-factor authentication.", 2),
            new Spec("Read the employee handbook", "Review the leave, conduct and security policies.", 5),
            new Spec("Meet your manager for a goal-setting chat", "Agree on your 30/60/90-day goals.", 7),
            new Spec("Complete security awareness training", "Finish the mandatory online course.", 10));

    private static final Map<String, List<Spec>> BY_DEPARTMENT = Map.of(
            "engineering", List.of(
                    new Spec("Clone the main repositories and run the app locally", "Follow the engineering setup guide.", 3),
                    new Spec("Ship your first small pull request", "Pick a 'good first issue' and get it reviewed.", 14)),
            "sales", List.of(
                    new Spec("Shadow three customer calls", "Listen in and note questions customers ask.", 7),
                    new Spec("Learn the CRM pipeline stages", "Complete the CRM walkthrough.", 5)),
            "hr", List.of(new Spec("Review the HR systems access checklist", "Confirm access to payroll and the HRIS.", 3)),
            "finance", List.of(new Spec("Get access to the expense and ERP systems", "Request access and complete the approver training.", 4)));

    /** Builds (does not save) the task list for an already-saved user. */
    public List<Task> generate(User user) {
        LocalDate start = user.getStartDate() != null ? user.getStartDate() : LocalDate.now();
        String dept = user.getDepartment() == null ? "" : user.getDepartment().trim().toLowerCase();
        List<Spec> specs = new ArrayList<>(COMMON);
        specs.addAll(BY_DEPARTMENT.getOrDefault(dept, List.of()));
        specs.sort(Comparator.comparingInt(Spec::dueInDays));
        List<Task> tasks = new ArrayList<>();
        for (Spec s : specs) {
            Task t = new Task();
            t.setTitle(s.title());
            t.setDescription(s.description());
            t.setDueDate(start.plusDays(s.dueInDays()));
            t.setUserId(user.getId());
            tasks.add(t);
        }
        return tasks;
    }
}
