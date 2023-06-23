package com.example.grocery_taxi.config;


import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

  private Map<String, String> secretKeyMap = new ConcurrentHashMap<>(); // Map to store secret keys for each user

  @Value("${jwt.expiration-time}")
  private long expirationTime;

  public String generateToken(String username) {
    String secretKey = getOrCreateSecretKey(username); // Get or create a secret key for the user

    // Build JWT token
    return Jwts.builder()
        .setHeaderParam(JwsHeader.TYPE, JwsHeader.JWT_TYPE)
        .setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      String username = extractUsernameFromToken(token);
      String secretKey = secretKeyMap.get(username); // Retrieve the secret key for the user

      if (secretKey != null) {
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .build()
            .parseClaimsJws(token);
        return true;
      }
    } catch (Exception e) {
      // Token validation failed
    }
    return false;
  }

  public String extractUsernameFromToken(String token) {
    return Jwts.parserBuilder()
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  private String getOrCreateSecretKey(String username) {
    return secretKeyMap.computeIfAbsent(username, key -> generateNewSecretKey());
  }

  private String generateNewSecretKey() {
    // Generate a new secret key
    byte[] keyBytes = new byte[64];
    new SecureRandom().nextBytes(keyBytes);
    return Base64.getEncoder().encodeToString(keyBytes);
  }
}

