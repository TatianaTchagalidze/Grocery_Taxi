package com.example.grocery_taxi.config;

import com.example.grocery_taxi.filter.JwtAuthenticationFilter;
import com.example.grocery_taxi.repository.UserRepository;
import com.example.grocery_taxi.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final CustomUserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationEntryPoint authenticationEntryPoint, JwtUtil jwtUtil, UserRepository userRepository) {
    this.userDetailsService = userDetailsService;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests()
        .antMatchers("/users").permitAll()
        .antMatchers("/health").permitAll()
        .antMatchers("/login").permitAll()
        .anyRequest().permitAll()
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
    return new JwtAuthenticationFilter(authenticationManager(), jwtUtil, userRepository);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
