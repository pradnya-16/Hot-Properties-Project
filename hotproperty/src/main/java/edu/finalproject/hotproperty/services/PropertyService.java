package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;

import java.util.List;
import java.util.Optional;

public interface PropertyService {
    List<Property> getPropertiesByAgent(User agent);
    void deleteProperty(Long propertyId, User agent);
}
