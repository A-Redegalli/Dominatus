package it.aredegalli.dominatus.service.auth;

import it.aredegalli.dominatus.dto.auth.*;
import it.aredegalli.dominatus.model.User;
import it.aredegalli.dominatus.repository.UserRepository;
import it.aredegalli.dominatus.security.jwt.EncryptionService;
import it.aredegalli.dominatus.security.jwt.JwtTokenProvider;
import it.aredegalli.dominatus.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        User user = userService.getDecryptedUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId().toString(), user.getEmail());

        return LoginResponseDto.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        User user = userService.createUser(
                registerRequestDto.getEmail(),
                registerRequestDto.getPlainPassword(),
                registerRequestDto.getFirstName(),
                registerRequestDto.getLastName()
        );

        String firstName = encryptionService.decrypt(user.getFirstName());
        String lastName = encryptionService.decrypt(user.getLastName());

        return RegisterResponseDto.builder()
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }


    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByEmail(changePasswordDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }

        String hashedNewPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
        user.setPassword(hashedNewPassword);
        userRepository.save(user);
        return true;
    }

}
