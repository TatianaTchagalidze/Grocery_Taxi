package com.example.grocery_taxi.filter;

import com.example.grocery_taxi.config.JwtUtil;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository) {
    super(authenticationManager);
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }


  public void saveTokenInCookies(HttpServletRequest request, HttpServletResponse response, String token) {
    Cookie jwtCookie = new Cookie("jwt", token);
    jwtCookie.setMaxAge(24 * 60 * 60); // Set the cookie expiration time (in seconds)
    jwtCookie.setPath("/"); // Set the cookie path
    response.addCookie(jwtCookie);
  }

  public void removeTokenFromCookies(HttpServletResponse response) {
    Cookie jwtCookie = new Cookie("jwt", "");
    jwtCookie.setMaxAge(0); // Set the cookie expiration time to 0 (to remove the cookie)
    jwtCookie.setPath("/"); // Set the cookie path
    response.addCookie(jwtCookie);
  }

  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String token = extractTokenFromCookies(request.getCookies());

    if (token != null && jwtUtil.validateToken(token)) {
      try {
        String username = jwtUtil.extractUsernameFromToken(token);
        User authenticatedUser = userRepository.findByEmail(username).orElse(null);
        if (authenticatedUser != null) {
          Set<GrantedAuthority> authorities = new HashSet<>();
          authorities.add(new SimpleGrantedAuthority(authenticatedUser.getRole().name()));

          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
              authenticatedUser, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          saveTokenInCookies(request, response, token);
        }
      } catch (Exception e) {
        removeTokenFromCookies(response);
      }
    }

    chain.doFilter(request, response);
  }

  public String extractTokenFromCookies(Cookie[] cookies) {
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("jwt")) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public User getAuthenticatedUserFromTokenInCookies(String token) {
    if (token != null && jwtUtil.validateToken(token)) {
      try {
        String username = jwtUtil.extractUsernameFromToken(token);
        return userRepository.findByEmail(username).orElse(null);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }
}