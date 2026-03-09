package com.oprm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private Integer userId;
    private String name;
    private String email;
    private String role;
}
