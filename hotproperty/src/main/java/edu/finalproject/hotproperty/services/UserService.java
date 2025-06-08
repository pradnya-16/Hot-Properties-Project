package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.dtos.UserProfileUpdateDto;
import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
  User registerBuyer(UserRegistrationDto registrationDto);

  User findByEmail(String email);

  boolean updateUserProfile(String currentEmail, UserProfileUpdateDto profileUpdateDto);

  User getCurrentUser(UserDetails userDetails);
}
