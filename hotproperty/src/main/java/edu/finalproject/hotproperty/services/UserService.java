package edu.finalproject.hotproperty.services;


import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.dtos.UserProfileUpdateDto;

public interface UserService {
    User registerBuyer(UserRegistrationDto registrationDto);
    User findByEmail(String email);
    boolean updateUserProfile(String currentEmail, UserProfileUpdateDto profileUpdateDto); 
}
