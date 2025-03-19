package it.aredegalli.dominatus.service.auth;

import io.jsonwebtoken.Claims;
import it.aredegalli.dominatus.dto.auth.changepassword.ChangePasswordDto;
import it.aredegalli.dominatus.dto.auth.login.LoginRequestDto;
import it.aredegalli.dominatus.dto.auth.login.LoginResponseDto;
import it.aredegalli.dominatus.dto.auth.refresh.RefreshRequestDto;
import it.aredegalli.dominatus.dto.auth.refresh.RefreshResponseDto;
import it.aredegalli.dominatus.dto.auth.register.RegisterRequestDto;
import it.aredegalli.dominatus.dto.auth.register.RegisterResponseDto;
import it.aredegalli.dominatus.enums.AuditEventTypeEnum;
import it.aredegalli.dominatus.model.RevokedToken;
import it.aredegalli.dominatus.model.User;
import it.aredegalli.dominatus.repository.RevokedTokenRepository;
import it.aredegalli.dominatus.repository.UserRepository;
import it.aredegalli.dominatus.security.encryption.EncryptionService;
import it.aredegalli.dominatus.security.jwt.JwtTokenProvider;
import it.aredegalli.dominatus.service.audit.AuditService;
import it.aredegalli.dominatus.service.user.UserService;
import it.aredegalli.dominatus.util.HashUtil;
import it.aredegalli.dominatus.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        User user = userService.getDecryptedUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        String ip = RequestUtil.getClientIp(httpServletRequest);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            auditService.logEvent(user, AuditEventTypeEnum.LOGIN_FAIL, "Dominatus", "Login failed: Invalid Password", Map.of(
                    "ipv4", ip,
                    "event", AuditEventTypeEnum.LOGIN_FAIL.name()));
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), user.getEmail(), null, this.initExtraClaims());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString(), user.getEmail(), null);

        auditService.logEvent(user, AuditEventTypeEnum.LOGIN, "Dominatus", "Login Success", Map.of(
                "ipv4", ip,
                "event", AuditEventTypeEnum.LOGIN.name()));

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), user.getEmail(), null, this.initExtraClaims());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString(), user.getEmail(), null);

        String ip = RequestUtil.getClientIp(httpServletRequest);
        auditService.logEvent(user, AuditEventTypeEnum.REGISTER, "Dominatus", "Register Success", Map.of(
                "ipv4", ip,
                "event", AuditEventTypeEnum.REGISTER.name()));

        return RegisterResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }


    @Override
    public boolean changePassword(ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByEmail(changePasswordDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String ip = RequestUtil.getClientIp(httpServletRequest);

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            auditService.logEvent(user, AuditEventTypeEnum.CHANGE_PASSWORD_FAIL, "Dominatus", "Change Password Failed: Incorrect Password", Map.of(
                    "ipv4", ip,
                    "event", AuditEventTypeEnum.CHANGE_PASSWORD_FAIL.name()));
            throw new RuntimeException("Invalid current password");
        }

        String hashedNewPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
        user.setPassword(hashedNewPassword);
        userRepository.save(user);

        auditService.logEvent(user, AuditEventTypeEnum.CHANGE_PASSWORD_SUCCESS, "Dominatus", "Chanage Password Success", Map.of(
                "ipv4", ip,
                "event", AuditEventTypeEnum.CHANGE_PASSWORD_SUCCESS.name()));
        return true;
    }


    @Override
    public RefreshResponseDto refreshToken(RefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String ip = RequestUtil.getClientIp(httpServletRequest);
        String tokenHash = HashUtil.sha256(refreshToken);

        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
            auditService.logEvent(null, AuditEventTypeEnum.TOKEN_REFRESH_FAIL, "Dominatus", "Refresh Replay Blocked", Map.of(
                    "ipv4", ip,
                    "event", AuditEventTypeEnum.TOKEN_REFRESH_FAIL.name(),
                    "refreshTokenHash", tokenHash));
            throw new RuntimeException("Refresh token already used");
        }

        Claims claims = jwtTokenProvider.getClaims(refreshToken);
        String userId = claims.getSubject();
        String email = claims.get("email", String.class);

        User user = userRepository.findById(UUID.fromString(userId)).orElse(null);
        revokedTokenRepository.save(RevokedToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(claims.getExpiration().toInstant())
                .build());

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, null, this.initExtraClaims());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, null);

        auditService.logEvent(user, AuditEventTypeEnum.TOKEN_REFRESH, "Dominatus", "Token Refresh", Map.of(
                "ipv4", ip,
                "event", AuditEventTypeEnum.TOKEN_REFRESH.name(),
                "usedRefreshTokenHash", tokenHash,
                "accessTokenHash", HashUtil.sha256(newAccessToken),
                "refreshTokenHash", HashUtil.sha256(newRefreshToken)));

        return RefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private Map<String, Object> initExtraClaims() {
        String ip = RequestUtil.getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("ipv4", ip);
        extraClaims.put("user-agent", userAgent);

        return extraClaims;
    }

}
