package it.aredegalli.dominatus.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final byte[] secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${security.jwt.secret-key}") String base64Secret,
            @Value("${security.jwt.expiration-time}") long expirationMillis) {
        this.secretKey = Base64.getDecoder().decode(base64Secret);
        this.expirationMillis = expirationMillis;
    }

    public String generateAccessToken(String userId, String email) {
        return generateToken(userId, email, expirationMillis);
    }

    public String generateRefreshToken(String userId, String email) {
        long refreshExpirationMillis = 7 * 24 * 60 * 60 * 1000L; // 7d
        return generateToken(userId, email, refreshExpirationMillis);
    }

    private String generateToken(String userId, String email, long expiryMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}
