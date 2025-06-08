package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.dtos.UserProfileUpdateDto;
import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.enums.RoleType;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User registerBuyer(UserRegistrationDto registrationDto) {
    if (userRepository.existsByEmail(registrationDto.getEmail())) {
      log.warn("Registration attempt with existing email: {}", registrationDto.getEmail());
      throw new InvalidUserParameterException(
          "Email already exists: " + registrationDto.getEmail());
    }
    if (registrationDto.getPassword() == null || registrationDto.getPassword().isEmpty()) {
      log.warn(
          "Registration attempt with empty password for email: {}", registrationDto.getEmail());
      throw new InvalidUserParameterException("Password cannot be empty.");
    }
    if (registrationDto.getFirstName() == null
        || registrationDto.getFirstName().isEmpty()
        || registrationDto.getLastName() == null
        || registrationDto.getLastName().isEmpty()) {
      log.warn(
          "Registration attempt with missing name fields for email: {}",
          registrationDto.getEmail());
      throw new InvalidUserParameterException("First name and last name are required.");
    }

    User newUser = new User();
    newUser.setFirstName(registrationDto.getFirstName());
    newUser.setLastName(registrationDto.getLastName());
    newUser.setEmail(registrationDto.getEmail());
    newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
    newUser.setRole(RoleType.BUYER);

    User savedUser = userRepository.save(newUser);
    log.info("New BUYER registered successfully: {}", savedUser.getEmail());
    return savedUser;
  }

  @Override
  public boolean updateUserProfile(String currentEmail, UserProfileUpdateDto profileUpdateDto) {
    User user =
        userRepository
            .findByEmail(currentEmail)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + currentEmail));

    boolean emailChanged = false;
    String newEmail = profileUpdateDto.getEmail();
    if (newEmail != null
        && !newEmail.trim().isEmpty()
        && !newEmail.equalsIgnoreCase(user.getEmail())) {
      if (userRepository.existsByEmail(newEmail)) {
        log.warn(
            "Attempt to update email to an existing one: {} by user {}", newEmail, currentEmail);
        throw new InvalidUserParameterException("Email already in use: " + newEmail);
      }
      user.setEmail(newEmail);
      emailChanged = true;
    }

    String newFirstName = profileUpdateDto.getFirstName();
    if (newFirstName != null && !newFirstName.trim().isEmpty()) {
      user.setFirstName(newFirstName);
    }

    String newLastName = profileUpdateDto.getLastName();
    if (newLastName != null && !newLastName.trim().isEmpty()) {
      user.setLastName(newLastName);
    }

    userRepository.save(user);
    log.info(
        "User profile updated for (original email): {}. Email changed: {}",
        currentEmail,
        emailChanged);
    return emailChanged;
  }

  @Override
  public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  @Override
  public User getCurrentUser(UserDetails userDetails) {
    if (userDetails == null) {
      log.warn("Attempted to fetch current user with null UserDetails.");
      throw new UsernameNotFoundException("User details cannot be null to fetch current user.");
    }
    return userRepository
        .findByEmail(userDetails.getUsername())
        .orElseThrow(
            () -> {
              log.warn("User not found with email from UserDetails: {}", userDetails.getUsername());
              return new UsernameNotFoundException("User not found: " + userDetails.getUsername());
            });
  }
}
