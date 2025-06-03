package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.User;

public interface BuyerService {
    User findByEmail(String email);
}
