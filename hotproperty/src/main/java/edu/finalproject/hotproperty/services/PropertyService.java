package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PropertyService {
  List<Property> getPropertiesByAgent(User agent);

  void deleteProperty(Long propertyId, User agent);

  List<Property> filterProperties(
      String zip, Integer minSqFt, Double minPrice, Double maxPrice, String sortBy);

  Property findWithImagesById(Long id);

  List<Property> getPropertiesByAgentWithImages(User agent);

  Property getPropertyWithAgentAndImagesById(Long propertyId);

  Property updateProperty(Long propertyId, Property propertyData, User agent);

  Property addProperty(Property propertyDetails, MultipartFile imageFile, User agent)
      throws IOException;

  void addImageToProperty(Long propertyId, MultipartFile imageFile, User agent) throws IOException;

  void removeImageFromProperty(Long propertyId, Long imageId, User agent) throws IOException;
}
