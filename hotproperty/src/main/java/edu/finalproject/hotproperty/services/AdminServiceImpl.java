package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.enums.RoleType;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

  private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
  private final UserRepository userRepository;
  private final PropertyRepository propertyRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public AdminServiceImpl(
      UserRepository userRepository,
      PropertyRepository propertyRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.propertyRepository = propertyRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    if (user.getRole().name().equals("AGENT")) {
      List<Property> properties = propertyRepository.findByAgent(user);
      propertyRepository.deleteAll(properties);
    }

    userRepository.delete(user);
  }

  @Override
  @Transactional
  public User createAgent(UserRegistrationDto registrationDto) {
    if (userRepository.existsByEmail(registrationDto.getEmail())) {
      log.warn("Create agent attempt with existing email: {}", registrationDto.getEmail());
      throw new InvalidUserParameterException(
          "Email already exists: " + registrationDto.getEmail());
    }
    if (registrationDto.getPassword() == null || registrationDto.getPassword().isEmpty()) {
      log.warn(
          "Create agent attempt with empty password for email: {}", registrationDto.getEmail());
      throw new InvalidUserParameterException("Password cannot be empty.");
    }

    if (registrationDto.getFirstName() == null
        || registrationDto.getFirstName().trim().isEmpty()
        || registrationDto.getLastName() == null
        || registrationDto.getLastName().trim().isEmpty()) {
      log.warn(
          "Create agent attempt with missing name fields for email: {}",
          registrationDto.getEmail());
      throw new InvalidUserParameterException("First name and last name are required.");
    }

    User newAgent = new User();
    newAgent.setFirstName(registrationDto.getFirstName().trim());
    newAgent.setLastName(registrationDto.getLastName().trim());
    newAgent.setEmail(registrationDto.getEmail().trim().toLowerCase());
    newAgent.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
    newAgent.setRole(RoleType.AGENT);

    User savedAgent = userRepository.save(newAgent);
    log.info("New AGENT created successfully: {}", savedAgent.getEmail());
    return savedAgent;
  }
}
