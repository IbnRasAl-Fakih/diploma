package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCodeDto {
    private String email;
    private String confirmationCode;
}
