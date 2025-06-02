
package edu.finalproject.hotproperty.services;


import edu.finalproject.hotproperty.dtos.UserRegistrationDto;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.enums.RoleType;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Handles user registration, different from customer user detsails service which is used for authentication
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
            throw new InvalidUserParameterException("Email already exists: " + registrationDto.getEmail());
        }
        if (registrationDto.getPassword() == null || registrationDto.getPassword().isEmpty()) {
            log.warn("Registration attempt with empty password for email: {}", registrationDto.getEmail());
            throw new InvalidUserParameterException("Password cannot be empty.");
        }
        if (registrationDto.getFirstName() == null || registrationDto.getFirstName().isEmpty() ||
            registrationDto.getLastName() == null || registrationDto.getLastName().isEmpty()) {
            log.warn("Registration attempt with missing name fields for email: {}", registrationDto.getEmail());
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
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
