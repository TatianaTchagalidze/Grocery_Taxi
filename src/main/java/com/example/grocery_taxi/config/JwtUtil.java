package com.example.grocery_taxi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
  @Value("${jwt.secret-length}")
  private int secretLength;

  @Value("${jwt.expiration-time}")
  private long expirationTime;

  public String generateToken(String username) {
    // Generate random secret
    byte[] secretBytes = generateRandomSecret();
    SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);

    // Build JWT token
    return Jwts.builder()
        .setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      byte[] secretBytes = Base64.getUrlDecoder().decode(getSecretFromToken(token));
      SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);

      Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private byte[] generateRandomSecret() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[secretLength];
    secureRandom.nextBytes(randomBytes);
    return randomBytes;
  }

  private String getSecretFromToken(String token) {
    String[] parts = token.split("\\.");
    return parts[0];
  }
}

