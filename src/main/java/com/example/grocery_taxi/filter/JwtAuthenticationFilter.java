package com.example.grocery_taxi.filter;

import com.example.grocery_taxi.config.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

  private final JwtUtil jwtUtil;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
    super(authenticationManager);
    this.jwtUtil = jwtUtil;
  }

  public void saveTokenInCookies(HttpServletRequest request, HttpServletResponse response,
                                 String token) {
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

  public String generateToken(String password) {
    return jwtUtil.generateToken(password);
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String token = extractTokenFromCookies(request.getCookies());

    if (token != null && jwtUtil.validateToken(token)) {
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(token, null);
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      saveTokenInCookies(request, response, token); // Save the token in cookies
    }

    chain.doFilter(request, response);
  }

  private String extractTokenFromCookies(Cookie[] cookies) {
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("jwt")) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
