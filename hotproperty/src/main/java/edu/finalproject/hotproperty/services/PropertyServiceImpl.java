package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Autowired
    public PropertyServiceImpl(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Property> getPropertiesByAgent(User agent) {
        return propertyRepository.findByAgent(agent);
    }

    @Override
    @Transactional
    public void deleteProperty(Long propertyId, User agent) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));


        if (!property.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Unauthorized to delete this property.");
        }
        propertyRepository.delete(property);
    }
}
