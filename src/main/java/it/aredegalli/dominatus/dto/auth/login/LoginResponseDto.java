package it.aredegalli.dominatus.dto.auth.login;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
}
