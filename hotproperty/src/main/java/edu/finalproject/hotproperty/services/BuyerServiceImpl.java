package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuyerServiceImpl implements BuyerService{
    private static final Logger log = LoggerFactory.getLogger(BuyerServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public BuyerServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> {
                    log.warn("User with email: {} not found", email);
                    return new InvalidUserParameterException(
                            "User with email: " + email + "not found");
                });
    }
}
