package it.aredegalli.dominatus.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
}
