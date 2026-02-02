package com.example.esm_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
@Tag(name = "Hello Controller", description = "API để kiểm tra Swagger")
public class HelloController {

    @GetMapping
    @Operation(summary = "Chào mừng", description = "Trả về một thông điệp chào mừng đơn giản.")
    public String sayHello() {
        return "Xin chào từ Spring Boot!";
    }
}
