package com.smartserve.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${smartserve.jwt.secret}")
    private String jwtSecret;

    @Value("${smartserve.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey(){

        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email, Long userId, String role){
        Date now= new Date();
        Date expiry= new Date(now.getTime() +jwtExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role !=null ? role: "STUDENT")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;

        }catch (ExpiredJwtException e){
            System.out.println("JWT expired: " + e.getMessage());
            return false;
        }catch (JwtException e){
            System.out.println("Invalid JWT: " + e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token){
        return getClaims(token).getSubject();
    }
    public Long extractUserId(String token){
        Object userId= getClaims(token).get("userId");
        if(userId instanceof Integer) return ((Integer) userId).longValue();
        if(userId instanceof Long) return (Long) userId;
        return null;
    }

    public String extractRole(String token){
        return (String) getClaims(token).get("role");
    }
    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
