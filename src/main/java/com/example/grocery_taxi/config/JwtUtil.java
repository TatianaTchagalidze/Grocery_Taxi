package com.example.grocery_taxi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {

  private Map<String, String> secretKeyMap = new ConcurrentHashMap<>(); // Map to store secret keys for each user

  @Value("${jwt.expiration-time}")
  private long expirationTime;

  public String generateToken(String username) {
    String secretKey = getOrCreateSecretKey(username); // Get or create a secret key for the user
    Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    return Jwts.builder()
        .setHeaderParam("kid", secretKey)
        .setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(key)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Key key = getKeyFromToken(token);
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String extractUsernameFromToken(String token) {
    Key key = getKeyFromToken(token);
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  private Key getKeyFromToken(String token) {
    String secretKey = secretKeyMap.get(Jwts.parser().parseClaimsJws(token).getHeader().get("kid"));
    return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
  }

  private String getOrCreateSecretKey(String username) {
    return secretKeyMap.computeIfAbsent(username, k -> generateSecretKey());
  }

  private String generateSecretKey() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] keyBytes = new byte[32];
    secureRandom.nextBytes(keyBytes);
    return Base64.getEncoder().encodeToString(keyBytes);
  }
}
