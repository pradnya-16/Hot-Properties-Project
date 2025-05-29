package edu.finalproject.hotproperty.services;


import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;

public interface UserService {
    User registerBuyer(UserRegistrationDto registrationDto);
    User findByEmail(String email);
}
