package com.dineflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    /**
     * Preserved API endpoint returning the welcome message.
     */
    @GetMapping("/api/welcome")
    public String welcome() {
        return "Welcome to Dineflow!";
    }

    /**
     * Backend status endpoint used by the frontend status badge.
     */
    @GetMapping("/api/status")
    public Map<String, Object> status() {
        return Map.of(
            "app", "Dineflow",
            "status", "UP",
            "message", "Welcome to Dineflow!"
        );
    }
}
