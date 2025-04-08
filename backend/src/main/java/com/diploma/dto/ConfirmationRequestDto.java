package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmationRequestDto {
    private String username;
    private String email;
    private String password;
}
