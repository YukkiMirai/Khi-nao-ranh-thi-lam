package com.wibuneverdie.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private static final String CLAIM_USER_UID = "userUid";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    // ------------------------------------------------------------------ //
    //  Token generation                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Tạo JWT chứa userId (subject) và userUid (claim).
     */
    public String generateToken(String userId, String userUid) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_USER_UID, userUid)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    // ------------------------------------------------------------------ //
    //  Token parsing                                                       //
    // ------------------------------------------------------------------ //

    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserUidFromToken(String token) {
        return parseClaims(token).get(CLAIM_USER_UID, String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
