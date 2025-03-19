package it.aredegalli.dominatus.security.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtKeyManager keyManager;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${security.jwt.expiration-time}") long expirationMillis,
            JwtKeyManager keyManager
    ) {
        this.expirationMillis = expirationMillis;
        this.keyManager = keyManager;
    }

    public String generateAccessToken(String userId, String email, String appName, Map<String, Object> extraClaims) {
        return generateToken(userId, email, appName, expirationMillis, extraClaims);
    }

    public String generateRefreshToken(String userId, String email, String appName) {
        long refreshExpirationMillis = 7 * 24 * 60 * 60 * 1000L; // 7d
        return generateToken(userId, email, appName, refreshExpirationMillis, null);
    }

    public String generateToken(String userId, String email, String appName, long expiryMillis, Map<String, Object> extraClaims) {
        SecretKey key = keyManager.getActiveKey();
        String kid = keyManager.getActiveKeyId();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        JwtBuilder builder = Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("app", appName)
                .setAudience(appName) // aud
                .setIssuer("dominatus-backend") // iss
                .setId(UUID.randomUUID().toString()) // jti
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setHeaderParam("k-id", kid)
                .signWith(key, SignatureAlgorithm.HS512);

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    public boolean validateToken(String token) {
        SecretKey key = keyManager.getActiveKey();

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        SecretKey key = keyManager.getActiveKey();
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
