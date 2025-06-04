package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyImageParameterException;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
  
    private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);

    @Autowired
    public PropertyServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public List<Property> getPropertiesByAgent(User agent) {
        if (agent == null) {
            throw new UsernameNotFoundException("Agent cannot be null for getting properties.");
        }
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

    @Override
    public Property findWithImagesById(Long id) {
        return propertyRepository.findWithImagesById(id).orElseThrow(
                () -> {
                    log.warn("Cannot find property image with id: {}", id);
                    return new InvalidPropertyImageParameterException(
                            "Cannot find property image with id:" + id);
                });
    }

    @Override
    public List<Property> filterProperties(String zip, Integer minSqFt, Double minPrice, Double maxPrice, String sortBy) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            log.warn("Minimum price {} cannot be greater than maximum price {}.", minPrice, maxPrice);
            throw new InvalidPropertyParameterException("Minimum price cannot be greater than maximum price.");
        }

        if (sortBy.equals("asc")) {
            return propertyRepository.filterPropertiesOrderByAsc(zip, minSqFt, minPrice, maxPrice);
        } else {
            return propertyRepository.filterPropertiesOrderByDesc(zip, minSqFt, minPrice, maxPrice);
        }
    }
}
