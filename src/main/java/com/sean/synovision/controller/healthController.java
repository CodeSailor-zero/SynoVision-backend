package com.sean.synovision.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sean
 * @Date 2025/42/20
 */
@RestController
@CrossOrigin(origins = {"http://localhost:8009"},allowCredentials = "true")
public class healthController {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
