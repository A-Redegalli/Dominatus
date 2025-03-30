package it.aredegalli.dominatus.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    private final long expirationMillis;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtTokenProvider(
            @Value("${security.jwt.expiration-time}") long expirationMillis,
            @Value("${security.jwt.private-key}") Resource privateKeyFile,
            @Value("${security.jwt.public-key}") Resource publicKeyFile
    ) {
        this.expirationMillis = expirationMillis;
        this.privateKey = loadPrivateKeyFromPem(privateKeyFile);
        this.publicKey = loadPublicKeyFromPem(publicKeyFile);
    }

    public String generateAccessToken(String userId, String email, String appName, Map<String, Object> extraClaims) {
        return generateToken(userId, email, appName, expirationMillis, extraClaims);
    }

    public String generateRefreshToken(String userId, String email, String appName) {
        long refreshExpirationMillis = 7 * 24 * 60 * 60 * 1000L; // 7 giorni
        return generateToken(userId, email, appName, refreshExpirationMillis, null);
    }

    private String generateToken(String userId, String email, String appName, long expiryMillis, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        JwtBuilder builder = Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("app", appName)
                .setAudience(appName)
                .setIssuer("dominatus-backend")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .signWith(privateKey, SignatureAlgorithm.RS256);

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    public RSAPrivateKey loadPrivateKeyFromPem(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            String pem = new String(is.readAllBytes())
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Errore durante il caricamento della chiave privata JWT", e);
        }
    }

    public RSAPublicKey loadPublicKeyFromPem(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            String pem = new String(is.readAllBytes())
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Errore durante il caricamento della chiave pubblica JWT", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
