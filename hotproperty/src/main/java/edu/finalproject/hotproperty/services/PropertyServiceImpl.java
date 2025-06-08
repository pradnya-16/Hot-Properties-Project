package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.PropertyImage;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.exceptions.PropertyImageManagementException;
import edu.finalproject.hotproperty.repositories.PropertyImageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PropertyServiceImpl implements PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;

  private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);
  private static final String PROPERTY_IMAGES_BASE_PATH =
      "src/main/resources/static/PropertyImages/";

  @Autowired
  public PropertyServiceImpl(
      PropertyRepository propertyRepository, PropertyImageRepository propertyImageRepository) {
    this.propertyRepository = propertyRepository;
    this.propertyImageRepository = propertyImageRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Property> getPropertiesByAgent(User agent) {
    if (agent == null) {
      throw new UsernameNotFoundException("Agent cannot be null for getting properties.");
    }
    return propertyRepository.findByAgent(agent);
  }

  @Override
  @Transactional
  public void deleteProperty(Long propertyId, User agent) {
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(
                () ->
                    new InvalidPropertyParameterException(
                        "Property not found with ID: " + propertyId));

    if (!property.getAgent().getId().equals(agent.getId())) {
      log.warn(
          "Agent {} attempted to delete property {} not belonging to them.",
          agent.getEmail(),
          propertyId);
      throw new AccessDeniedException("Unauthorized to delete this property.");
    }

    String titleForFolder = property.getTitle();
    Path folderPath = Paths.get(PROPERTY_IMAGES_BASE_PATH, titleForFolder);
    try {
      if (Files.exists(folderPath)) {
        Files.walk(folderPath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        log.info("Successfully deleted image folder: {}", folderPath);
      }
    } catch (IOException e) {
      log.error(
          "Could not delete image folder {} for property ID {}: {}. Property not deleted.",
          folderPath,
          propertyId,
          e.getMessage());
      throw new PropertyImageManagementException(
          "Failed to delete property images. Property not deleted. Error: " + e.getMessage(), e);
    }

    propertyRepository.delete(property);
    log.info(
        "Property with ID {} and its DB associations deleted by agent {}.",
        propertyId,
        agent.getEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public Property findWithImagesById(Long id) {
    return propertyRepository
        .findWithImagesById(id)
        .orElseThrow(
            () -> {
              log.warn("Cannot find property with id: {} (with images)", id);
              return new InvalidPropertyParameterException("Cannot find property with id:" + id);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public List<Property> filterProperties(
      String zip, Integer minSqFt, Double minPrice, Double maxPrice, String sortBy) {
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
      log.warn("Minimum price {} cannot be greater than maximum price {}.", minPrice, maxPrice);
      throw new InvalidPropertyParameterException(
          "Minimum price cannot be greater than maximum price.");
    }

    if ("asc".equalsIgnoreCase(sortBy)) {
      return propertyRepository.filterPropertiesOrderByAsc(zip, minSqFt, minPrice, maxPrice);
    } else {
      return propertyRepository.filterPropertiesOrderByDesc(zip, minSqFt, minPrice, maxPrice);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Property> getPropertiesByAgentWithImages(User agent) {
    if (agent == null) {
      throw new UsernameNotFoundException("Agent cannot be null for getting properties.");
    }
    return propertyRepository.findWithImagesByAgent(agent);
  }

  @Override
  @Transactional(readOnly = true)
  public Property getPropertyWithAgentAndImagesById(Long propertyId) {
    return propertyRepository
        .findWithAgentAndImagesById(propertyId)
        .orElseThrow(
            () ->
                new InvalidPropertyParameterException("Property not found with ID: " + propertyId));
  }

  @Override
  @Transactional
  public Property updateProperty(Long propertyId, Property propertyData, User agent) {
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(
                () ->
                    new InvalidPropertyParameterException(
                        "Property not found with ID: " + propertyId));

    if (!property.getAgent().getId().equals(agent.getId())) {
      log.warn(
          "Agent {} attempted to update property {} not belonging to them.",
          agent.getEmail(),
          propertyId);
      throw new AccessDeniedException("You are not authorized to update this property.");
    }

    String oldTitle = property.getTitle();
    String newTitle = propertyData.getTitle();

    if (newTitle == null || newTitle.trim().isEmpty()) {
      throw new InvalidPropertyParameterException("Property title cannot be empty.");
    }
    newTitle = newTitle.trim();

    boolean titleChanged = !newTitle.equals(oldTitle);

    if (titleChanged) {
      Path oldFolderPath = Paths.get(PROPERTY_IMAGES_BASE_PATH, oldTitle);
      Path newFolderPath = Paths.get(PROPERTY_IMAGES_BASE_PATH, newTitle);

      if (Files.exists(oldFolderPath) && !oldFolderPath.equals(newFolderPath)) {
        try {
          if (newFolderPath.getParent() != null && !Files.exists(newFolderPath.getParent())) {
            Files.createDirectories(newFolderPath.getParent());
          }
          Files.move(oldFolderPath, newFolderPath, StandardCopyOption.REPLACE_EXISTING);
          log.info("Renamed property image folder from {} to {}", oldFolderPath, newFolderPath);
        } catch (IOException e) {
          log.error(
              "Could not rename image folder from {} to {}: {}. Property update failed.",
              oldFolderPath,
              newFolderPath,
              e.getMessage());
          throw new PropertyImageManagementException(
              "Failed to update image storage due to title change. Property not updated. Error: "
                  + e.getMessage(),
              e);
        }
      } else if (!Files.exists(oldFolderPath)) {
        log.warn(
            "Old image folder {} did not exist for property ID {}, no rename performed for image"
                + " directory.",
            oldFolderPath,
            propertyId);
      }
    }

    property.setTitle(newTitle);
    property.setLocation(propertyData.getLocation());
    property.setPrice(propertyData.getPrice());
    property.setSize(propertyData.getSize());
    property.setDescription(propertyData.getDescription());

    Property updatedProperty = propertyRepository.save(property);
    log.info("Property entity with ID {} updated by agent {}.", propertyId, agent.getEmail());
    return updatedProperty;
  }

  @Override
  @Transactional
  public Property addProperty(Property propertyDetails, MultipartFile imageFile, User agent) {
    propertyDetails.setAgent(agent);
    if (propertyDetails.getTitle() == null || propertyDetails.getTitle().trim().isEmpty()) {
      throw new InvalidPropertyParameterException("Property title cannot be empty.");
    }
    String titleForFolder = propertyDetails.getTitle();

    Property savedProperty = propertyRepository.save(propertyDetails);
    log.info(
        "Property entity with title '{}' saved by agent {}.",
        savedProperty.getTitle(),
        agent.getEmail());

    if (imageFile != null && !imageFile.isEmpty()) {
      String folderPathStr = PROPERTY_IMAGES_BASE_PATH + titleForFolder;
      Path folderPath = Paths.get(folderPathStr);

      try {
        if (!Files.exists(folderPath)) {
          Files.createDirectories(folderPath);
          log.info("Created directory for property images: {}", folderPath);
        }

        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
          originalFilename =
              "property_image_" + System.currentTimeMillis() + "_" + imageFile.hashCode();
        }
        Path imagePath = folderPath.resolve(originalFilename);
        Files.write(imagePath, imageFile.getBytes());

        PropertyImage propertyImage = new PropertyImage(originalFilename, savedProperty);
        propertyImageRepository.save(propertyImage);
        log.info("Image {} saved for property ID {}", originalFilename, savedProperty.getId());
      } catch (IOException e) {
        log.error("Could not save image for property title {}: {}", titleForFolder, e.getMessage());
        throw new PropertyImageManagementException(
            "Failed to save property image. Error: " + e.getMessage(), e);
      }
    }
    return savedProperty;
  }
}
