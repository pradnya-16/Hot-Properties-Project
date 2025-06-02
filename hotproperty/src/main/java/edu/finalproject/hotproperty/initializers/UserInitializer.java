package edu.finalproject.hotproperty.initializers;

import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.enums.RoleType;
import edu.finalproject.hotproperty.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class UserInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
  @Transactional
    public void init() {
        if(userRepository.count() == 0) {
            User admin = new User("Samuel", "Admin", "admin@example.com", passwordEncoder.encode("admin123"), RoleType.ADMIN);
            User agent1 = new User("Pradnya", "Agent", "agent1@example.com", passwordEncoder.encode("agent123"), RoleType.AGENT);
            User agent2 = new User("Shubham", "Agent", "agent2@example.com", passwordEncoder.encode("agent456"), RoleType.AGENT);
            User buyer = new User("Jeff", "Buyer", "buyer@example.com", passwordEncoder.encode("buyer123"), RoleType.BUYER);

            userRepository.save(admin);
            userRepository.save(agent1);
            userRepository.save(agent2);
            userRepository.save(buyer);

            System.out.println("Sample users initialized.");
        }
    }
}
