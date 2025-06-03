package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import java.util.List;

public interface AdminService {
  List<User> getAllUsers();

  void deleteUser(Long userId);

  User createAgent(UserRegistrationDto registrationDto);
}
