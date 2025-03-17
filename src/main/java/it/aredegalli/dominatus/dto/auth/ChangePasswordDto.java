package it.aredegalli.dominatus.dto.auth;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String email;
    private String oldPassword;
    private String newPassword;
}
