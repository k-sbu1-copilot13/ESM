package com.example.esm_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private Long id;
    private String username;
    private String fullName;
    private String role;
    private String status;
}
