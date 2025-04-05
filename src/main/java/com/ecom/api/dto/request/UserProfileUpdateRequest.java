package com.ecom.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateRequest {

  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  private String firstName;

  @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
  private String lastName;

  @Email(message = "Please provide a valid email address")
  private String email;

  @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid with optional + prefix")
  private String phoneNumber;

  private String address;

  private String city;

  private String state;

  @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$", message = "Zip code must be in format 12345 or 12345-6789")
  private String zipCode;

  private String country;
}
