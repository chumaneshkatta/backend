package com.anurag.ai.web;

import com.anurag.ai.model.User;
import com.anurag.ai.repository.UserRepository;
import com.anurag.ai.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT + RBAC gate for /api/**. The role is always read from the database, never from the token,
 * so demoting or deleting a user takes effect immediately.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String CURRENT_USER = "currentUser";
    private final JwtService jwt;
    private final UserRepository users;

    public AuthInterceptor(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod hm)) return true; // CORS preflight, static resources

        User user = null;
        String header = req.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            user = jwt.parseUserId(header.substring(7).trim())
                    .flatMap(users::findById).filter(User::isActive).orElse(null);
        }
        boolean isPublic = hm.hasMethodAnnotation(Public.class);
        if (user == null && !isPublic) return fail(res, 401, "Not authenticated or token expired");

        boolean adminOnly = hm.hasMethodAnnotation(RequireAdmin.class)
                || hm.getBeanType().isAnnotationPresent(RequireAdmin.class);
        if (adminOnly && (user == null || !User.ADMIN.equals(user.getRole()))) {
            return fail(res, 403, "Admin access required");
        }
        if (user != null) req.setAttribute(CURRENT_USER, user);
        return true;
    }

    private boolean fail(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        if (status == 401) res.setHeader("WWW-Authenticate", "Bearer");
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"message\":\"" + message + "\"}");
        return false;
    }
}
