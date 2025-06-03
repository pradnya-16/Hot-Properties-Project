package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (user.getRole().name().equals("AGENT")) {
            List<Property> properties = propertyRepository.findByAgent(user);
            propertyRepository.deleteAll(properties);
        }

        userRepository.delete(user);
    }
}
