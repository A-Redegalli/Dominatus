package it.aredegalli.dominatus.service.auth;

import it.aredegalli.dominatus.dto.auth.changepassword.ChangePasswordDto;
import it.aredegalli.dominatus.dto.auth.login.LoginRequestDto;
import it.aredegalli.dominatus.dto.auth.login.LoginResponseDto;
import it.aredegalli.dominatus.dto.auth.refresh.RefreshRequestDto;
import it.aredegalli.dominatus.dto.auth.refresh.RefreshResponseDto;
import it.aredegalli.dominatus.dto.auth.register.RegisterRequestDto;
import it.aredegalli.dominatus.dto.auth.register.RegisterResponseDto;

public interface AuthService {

    /**
     * Login a user
     *
     * @param loginRequest
     * @return the response of the login, with JWT token
     */
    LoginResponseDto login(LoginRequestDto loginRequest);

    /**
     * Register a new user
     *
     * @param registerRequestDto
     * @return the response of the registration, with JWT token
     */
    RegisterResponseDto register(RegisterRequestDto registerRequestDto);


    /**
     * Change the password of a user
     *
     * @param changePasswordDto
     * @return true if the password was changed, false otherwise
     */
    boolean changePassword(ChangePasswordDto changePasswordDto);

    RefreshResponseDto refreshToken(RefreshRequestDto request);
}
