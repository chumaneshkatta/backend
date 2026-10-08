package com.anurag.ai.service;

import com.anurag.ai.web.Dto.ChatResponse;
import com.anurag.ai.web.Dto.ChatSource;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Retrieval over the company policy library: the question is matched to policy documents by
 * keyword coverage and the best match is returned as the answer. No external vector DB or LLM.
 */
@Service
public class RagService {
    public record Policy(String title, String text) {}

    public static final String NOT_FOUND =
            "I couldn't find that in the onboarding policies. Please ask your HR contact or manager.";
    private static final double MIN_SCORE = 0.2;
    private static final Set<String> STOP = Set.of("a", "an", "the", "is", "are", "am", "do", "does", "did", "i", "me", "my",
            "we", "you", "your", "to", "of", "for", "in", "on", "at", "and", "or", "how", "what", "when", "where", "who",
            "can", "get", "much", "many", "be", "it", "this", "that", "with", "about");

    private static final List<Policy> POLICIES = List.of(
            new Policy("Annual leave", "Full-time employees get 24 vacation days per year, accrued monthly. Request leave in the HR portal at least two weeks ahead and get your manager's approval."),
            new Policy("Sick leave", "You get 10 paid sick days per year. Tell your manager before your shift starts. A doctor's note is needed for absences longer than three days."),
            new Policy("Remote work", "Hybrid working is the default: three days in the office and two days remote. Fully remote arrangements need written approval from your manager and HR."),
            new Policy("Working hours", "Core hours are 10:00 to 16:00 local time. Standard working hours are 9:00 to 17:30 with a 30 minute unpaid lunch break."),
            new Policy("Expenses", "Submit expense claims within 30 days with receipts attached. Travel must be booked through the company travel tool. Claims over 500 USD need manager approval."),
            new Policy("IT security", "Enable two-factor authentication on all work accounts. Never share passwords. Report lost devices or phishing emails to the security team immediately."),
            new Policy("Health benefits", "Health, dental and vision insurance start on your first day. Enrol within 30 days of joining using the benefits portal. Dependants can be added during enrolment."),
            new Policy("Payroll", "Salaries are paid on the last working day of each month. Payslips are available in the HR portal. Contact payroll for any discrepancy."),
            new Policy("Probation and reviews", "The probation period is three months with check-ins at 30, 60 and 90 days. Performance reviews happen twice a year."));

    static Set<String> tokens(String text) {
        return Arrays.stream(text.toLowerCase().split("[^a-z0-9]+"))
                .filter(w -> !w.isEmpty() && !STOP.contains(w))
                .map(w -> w.length() > 3 && w.endsWith("s") ? w.substring(0, w.length() - 1) : w)
                .collect(Collectors.toSet());
    }

    private record Scored(Policy policy, double score) {}

    public ChatResponse answer(String question) {
        Set<String> q = tokens(question);
        if (q.isEmpty()) return new ChatResponse(NOT_FOUND, List.of());
        List<Scored> hits = POLICIES.stream()
                .map(p -> {
                    Set<String> doc = tokens(p.title() + " " + p.text());
                    long overlap = q.stream().filter(doc::contains).count();
                    return new Scored(p, (double) overlap / q.size());
                })
                .filter(s -> s.score() >= MIN_SCORE)
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(3).toList();
        if (hits.isEmpty()) return new ChatResponse(NOT_FOUND, List.of());
        String answer = hits.get(0).policy().text();
        if (hits.size() > 1) {
            answer += " Related topics: " + hits.stream().skip(1).map(s -> s.policy().title()).collect(Collectors.joining(", ")) + ".";
        }
        List<ChatSource> sources = hits.stream().map(s -> new ChatSource(s.policy().title(),
                s.policy().text().substring(0, Math.min(140, s.policy().text().length())))).toList();
        return new ChatResponse(answer, sources);
    }
}
