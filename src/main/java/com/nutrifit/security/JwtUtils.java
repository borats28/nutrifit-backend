package com.nutrifit.security;

import com.nutrifit.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${nutrifit.app.jwtSecret}")
    private String jwtSecret;

    @Value("${nutrifit.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // JWT token oluşturma
    public String generateJwtToken(Authentication authentication) {
        // Giriş yapan kullanıcı bilgisini al
        User userPrincipal = (User) authentication.getPrincipal();

        // Token'ı yarat
        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // Token'ın içine kullanıcı adını göm
                .issuedAt(new Date()) // Oluşturulma tarihi şu an
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Bitiş tarihi
                .signWith(key(), Jwts.SIG.HS256) // Gizli anahtarımızla imzala
                .compact();
    }

    // Gizli anahtarı çözme
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Tokendan kullanıcı adını alma
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Token geçerlimi değil mi doğrulama
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Geçersiz JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Süresi dolmuş JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Desteklenmeyen JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims dizisi boş: {}", e.getMessage());
        }

        return false;
    }
}