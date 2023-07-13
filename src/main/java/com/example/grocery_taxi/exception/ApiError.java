package com.example.grocery_taxi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiError {
  private HttpStatus status;
  private LocalDateTime timestamp;
  private String message;
  private List<String> subErrors;

}
