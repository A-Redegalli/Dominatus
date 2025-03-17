package it.aredegalli.dominatus.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String email;
    private String plainPassword;
    private String firstName;
    private String lastName;
}
