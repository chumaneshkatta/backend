package com.anurag.ai.service;

import com.anurag.ai.model.User;
import com.anurag.ai.repository.UserRepository;
import com.anurag.ai.web.ApiException;
import com.anurag.ai.web.Dto.*;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("timing-equaliser");
    }

    /** bcrypt only reads 72 bytes; reject longer input rather than silently truncating it. */
    public static void checkPassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "password: must be at most 72 bytes");
        }
    }

    @Transactional
    public UserView register(RegisterRequest req, User caller) {
        String role = req.role() == null ? User.USER : req.role();
        if (User.ADMIN.equals(role) && users.countByRole(User.ADMIN) > 0
                && (caller == null || !User.ADMIN.equals(caller.getRole()))) {
            // Without this check anyone could create themselves an admin account.
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin accounts can only be created by an existing admin");
        }
        checkPassword(req.password());
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(role);
        u.setDepartment(req.department());
        u.setPosition(req.position());
        return com.anurag.ai.web.Dto.userView(users.save(u));
    }

    public TokenResponse login(String email, String password) {
        User u = users.findByEmail(email == null ? "" : email.trim().toLowerCase()).orElse(null);
        boolean ok = encoder.matches(password, u == null ? dummyHash : u.getPasswordHash()); // same cost either way
        if (u == null || !ok || !u.isActive()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
        }
        return new TokenResponse(jwt.generate(u), "bearer");
    }
}
