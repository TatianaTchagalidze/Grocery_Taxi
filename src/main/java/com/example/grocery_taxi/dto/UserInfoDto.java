package com.example.grocery_taxi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDto {
  private String email;
  private String firstName;
  private String lastName;
  private String address;
  private String phoneNumber;
  private String role;

  public UserInfoDto(UserDto userDto) {
    this.email = userDto.getEmail();
    this.firstName = userDto.getFirstName();
    this.lastName = userDto.getLastName();
    this.address = userDto.getAddress();
    this.phoneNumber = userDto.getPhoneNumber();
    this.role = userDto.getRole();
  }
}
