package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);

    @Autowired
    public PropertyServiceImpl (PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public Property findWithImagesById(Long id) {
        return propertyRepository.findWithImagesById(id).orElseThrow();
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
