package com.anurag.ai.web;

import com.anurag.ai.model.User;
import com.anurag.ai.service.AuthService;
import com.anurag.ai.web.Dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @Public
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView register(@Valid @RequestBody RegisterRequest req,
                             @RequestAttribute(name = AuthInterceptor.CURRENT_USER, required = false) User caller) {
        return auth.register(req, caller);
    }

    @Public
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req.email(), req.password());
    }

    @Public
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse loginForm(@RequestParam(required = false) String email,
                                   @RequestParam(required = false) String username,
                                   @RequestParam String password) {
        return auth.login(email != null ? email : username, password);
    }

    @GetMapping("/me")
    public UserView me(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        return Dto.userView(user);
    }
}
