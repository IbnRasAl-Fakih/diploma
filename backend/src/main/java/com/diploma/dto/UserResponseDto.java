package com.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
}
