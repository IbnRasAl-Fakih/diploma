package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmailUpdateRequestDto {
    private UUID userId;
    private String newEmail;
}
