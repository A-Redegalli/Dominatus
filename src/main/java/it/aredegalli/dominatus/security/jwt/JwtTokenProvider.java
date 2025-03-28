package it.aredegalli.dominatus.security.jwt;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    private final long expirationMillis;
    private final PrivateKey privateKey;

    public JwtTokenProvider(
            @Value("${security.jwt.expiration-time}") long expirationMillis,
            @Value("${security.jwt.private-key}") Resource keyFile
    ) {
        this.expirationMillis = expirationMillis;
        this.privateKey = loadPrivateKeyFromPem(keyFile);
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

    private PrivateKey loadPrivateKeyFromPem(Resource pemFile) {
        try {
            String pem = new String(pemFile.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Decoders.BASE64.decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Errore durante il caricamento della chiave privata JWT", e);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella costruzione della PrivateKey", e);
        }
    }
}
