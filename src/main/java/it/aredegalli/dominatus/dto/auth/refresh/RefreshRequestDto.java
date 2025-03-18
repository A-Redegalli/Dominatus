package it.aredegalli.dominatus.dto.auth.refresh;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequestDto {

    @NotBlank(message = "Refresh token is mandatory")
    private String refreshToken;
}
