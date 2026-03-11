package com.urlshortener.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA fallback — only active in the "prod" profile.
 *
 * In PROD: Vue's dist/ is copied into src/main/resources/static/.
 * When user refreshes /dashboard, Spring gets the request and forwards
 * to index.html so Vue Router handles it client-side.
 *
 * In DEV: this controller is NOT loaded. Vite (port 5173) handles all
 * frontend routes itself — no forwarding needed, no redirect loops.
 *
 * To activate: run with --spring.profiles.active=prod
 */
@Profile("prod")
@Controller
public class SpaFallbackController {

    @GetMapping({"/", "/login", "/register", "/dashboard", "/analytics/**"})
    public String spa(HttpServletRequest request) {
        return "forward:/index.html";
    }
}
