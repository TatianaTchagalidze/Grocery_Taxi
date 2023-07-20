package com.example.grocery_taxi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationResponseDto {
  private int id;
  private String email;
  private String firstName;
  private String lastName;
  private String address;
  private String phone_number;
  private String role;

  public RegistrationResponseDto(UserDto userDto, int id ) {
    this.id = id;
    this.email = userDto.getEmail();
    this.firstName = userDto.getFirstName();
    this.lastName = userDto.getLastName();
    this.address = userDto.getAddress();
    this.phone_number = userDto.getPhoneNumber();
    this.role = userDto.getRole();
  }
}

