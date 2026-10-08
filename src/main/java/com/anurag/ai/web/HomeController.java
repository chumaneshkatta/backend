package com.anurag.ai.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {
    private final String frontendUrl;

    public HomeController(@Value("${app.frontend.url:http://127.0.0.1:5173}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView(frontendUrl);
    }
}
