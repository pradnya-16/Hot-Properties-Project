package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;

import java.util.List;

public interface PropertyService {
    List<Property> filterProperties(String zip, Integer minSqFt, Double minPrice, Double maxPrice, String sortBy);
    Property findWithImagesById(Long id);
}
