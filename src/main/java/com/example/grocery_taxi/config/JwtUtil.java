package com.example.grocery_taxi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;


import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

  private String secretKey;

  @Value("${jwt.expiration-time}")
  private long expirationTime;

  public JwtUtil(@Value("${jwt.secret-key}") String secretKey) {
    this.secretKey = secretKey;
  }

  public String generateToken(String username) {
    Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    return Jwts.builder()
        .setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expirationTime)))
        .signWith(key)
        .compact();
  }


  public boolean validateToken(String token) {
    Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException | UnsupportedJwtException e) {
      return false;
    }
  }


  public String extractUsernameFromToken(String token) {
    Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

}
