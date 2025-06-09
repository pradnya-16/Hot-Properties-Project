package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.PropertyImage;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyImageParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.exceptions.PropertyImageManagementException;
import edu.finalproject.hotproperty.repositories.FavoriteRepository;
import edu.finalproject.hotproperty.repositories.PropertyImageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PropertyServiceImpl implements PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final FavoriteRepository favoriteRepository;

  private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);
  private static final String PROPERTY_IMAGES_BASE_PATH = "uploads/properties/";

  @Autowired
  public PropertyServiceImpl(
      PropertyRepository propertyRepository,
      PropertyImageRepository propertyImageRepository,
      FavoriteRepository favoriteRepository) {
    this.propertyRepository = propertyRepository;
    this.propertyImageRepository = propertyImageRepository;
    this.favoriteRepository = favoriteRepository;
  }

  private String getBaseName(String fileName) {
    if (fileName == null || fileName.isEmpty()) return "image";
    int lastDot = fileName.lastIndexOf('.');
    return (lastDot > 0) ? fileName.substring(0, lastDot) : fileName;
  }

  private String getExtension(String fileName) {
    if (fileName == null || fileName.isEmpty()) return ".jpg";
    int lastDot = fileName.lastIndexOf('.');
    return (lastDot >= 0 && lastDot < fileName.length() - 1) ? fileName.substring(lastDot) : "";
  }

  private String generateUniqueFilenameWithUUID(String originalFilename) {
    String cleanedOriginalFilename = StringUtils.cleanPath(originalFilename);
    String baseName = getBaseName(cleanedOriginalFilename);
    String extension = getExtension(cleanedOriginalFilename);
    String uuid = UUID.randomUUID().toString();
    return baseName + "_" + uuid + extension;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Property> getPropertiesByAgent(User agent) {
    if (agent == null) throw new UsernameNotFoundException("Agent cannot be null.");
    return propertyRepository.findByAgent(agent);
  }

  @Override
  @Transactional
  public void deleteProperty(Long propertyId, User agent) {
    Property property =
        propertyRepository
            .findWithImagesById(propertyId)
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

    List<String> imageFileNamesToDelete =
        property.getImages().stream().map(PropertyImage::getImageFileName).toList();

    propertyRepository.delete(property);

    for (String fileName : imageFileNamesToDelete) {
      Path imagePath = Paths.get(PROPERTY_IMAGES_BASE_PATH, fileName);
      try {
        Files.deleteIfExists(imagePath);
        log.info("Image file {} deleted from filesystem for property ID {}.", fileName, propertyId);
      } catch (IOException e) {
        log.error(
            "Could not delete image file {} for property ID {} from filesystem: {}",
            fileName,
            propertyId,
            e.getMessage(),
            e);
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Property findWithImagesById(Long id) {
    return propertyRepository
        .findWithImagesById(id)
        .orElseThrow(
            () -> new InvalidPropertyParameterException("Cannot find property with id:" + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Property> filterProperties(
      String zip, Integer minSqFt, Double minPrice, Double maxPrice, String sortBy) {
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
      throw new InvalidPropertyParameterException(
          "Minimum price cannot be greater than maximum price.");
    }
    return "asc".equalsIgnoreCase(sortBy)
        ? propertyRepository.filterPropertiesOrderByAsc(zip, minSqFt, minPrice, maxPrice)
        : propertyRepository.filterPropertiesOrderByDesc(zip, minSqFt, minPrice, maxPrice);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Property> getPropertiesByAgentWithImages(User agent) {
    if (agent == null) throw new UsernameNotFoundException("Agent cannot be null.");
    List<Property> properties = propertyRepository.findWithImagesByAgent(agent);
    properties.forEach(
        prop -> prop.setFavoriteCount((int) favoriteRepository.countByPropertyId(prop.getId())));
    return properties;
  }

  @Override
  @Transactional(readOnly = true)
  public Property getPropertyWithAgentAndImagesById(Long propertyId) {
    return propertyRepository
        .findWithImagesById(propertyId)
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
      throw new AccessDeniedException("You are not authorized to update this property.");
    }
    String newTitle = propertyData.getTitle();
    if (newTitle == null || newTitle.trim().isEmpty())
      throw new InvalidPropertyParameterException("Property title cannot be empty.");

    property.setTitle(newTitle.trim());
    property.setLocation(propertyData.getLocation());
    property.setPrice(propertyData.getPrice());
    property.setSize(propertyData.getSize());
    property.setDescription(propertyData.getDescription());
    return propertyRepository.save(property);
  }

  @Override
  @Transactional
  public Property addProperty(Property propertyDetails, MultipartFile imageFile, User agent)
      throws IOException {
    propertyDetails.setAgent(agent);
    if (propertyDetails.getTitle() == null || propertyDetails.getTitle().trim().isEmpty()) {
      throw new InvalidPropertyParameterException("Property title cannot be empty.");
    }
    Property savedProperty = propertyRepository.save(propertyDetails);

    if (imageFile != null && !imageFile.isEmpty()) {
      Path imageStorageFolder = Paths.get(PROPERTY_IMAGES_BASE_PATH);
      try {
        if (!Files.exists(imageStorageFolder)) Files.createDirectories(imageStorageFolder);
        String finalFilename = generateUniqueFilenameWithUUID(imageFile.getOriginalFilename());
        Path imagePath = imageStorageFolder.resolve(finalFilename);
        Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

        PropertyImage propertyImage = new PropertyImage(finalFilename, savedProperty);
        savedProperty.getImages().add(propertyImage);
        propertyRepository.save(savedProperty);
      } catch (IOException e) {
        log.error(
            "Could not save image for property title {}: {}",
            propertyDetails.getTitle(),
            e.getMessage(),
            e);
        throw new PropertyImageManagementException(
            "Failed to save property image: " + e.getMessage(), e);
      }
    }
    return savedProperty;
  }

  @Override
  @Transactional
  public void addImageToProperty(Long propertyId, MultipartFile imageFile, User agent)
      throws IOException {
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(
                () ->
                    new InvalidPropertyParameterException(
                        "Property not found with ID: " + propertyId));

    if (!property.getAgent().getId().equals(agent.getId())) {
      throw new AccessDeniedException("Unauthorized to modify this property's images.");
    }
    if (imageFile == null || imageFile.isEmpty())
      throw new InvalidPropertyImageParameterException("Image file cannot be empty.");

    Path imageStorageFolder = Paths.get(PROPERTY_IMAGES_BASE_PATH);
    if (!Files.exists(imageStorageFolder)) Files.createDirectories(imageStorageFolder);

    String finalFilename = generateUniqueFilenameWithUUID(imageFile.getOriginalFilename());
    Path imagePath = imageStorageFolder.resolve(finalFilename);
    Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

    PropertyImage propertyImage = new PropertyImage(finalFilename, property);
    property.getImages().add(propertyImage);
    propertyRepository.save(property);
  }

  @Override
  @Transactional
  public void removeImageFromProperty(Long propertyId, Long imageId, User agent)
      throws IOException {
    Property property =
        propertyRepository
            .findWithImagesById(propertyId)
            .orElseThrow(
                () ->
                    new InvalidPropertyParameterException(
                        "Property not found with ID: " + propertyId));

    if (!property.getAgent().getId().equals(agent.getId())) {
      throw new AccessDeniedException("Unauthorized to modify this property's images.");
    }

    PropertyImage imageToRemove =
        property.getImages().stream()
            .filter(img -> img.getId().equals(imageId))
            .findFirst()
            .orElseThrow(
                () ->
                    new InvalidPropertyImageParameterException(
                        "Image with ID: " + imageId + " not found for this property."));

    String fileNameToDelete = imageToRemove.getImageFileName();

    boolean removedFromCollection = property.getImages().remove(imageToRemove);

    if (removedFromCollection) {
      propertyRepository.save(property);

      Path imagePath = Paths.get(PROPERTY_IMAGES_BASE_PATH, fileNameToDelete);
      try {
        Files.deleteIfExists(imagePath);
        log.info("Image file {} deleted from filesystem.", fileNameToDelete);
      } catch (IOException e) {
        log.error(
            "Could not delete image file {} from filesystem for property ID {}: {}",
            fileNameToDelete,
            propertyId,
            e.getMessage(),
            e);
        throw new PropertyImageManagementException(
            "DB record for image "
                + fileNameToDelete
                + " removed, but failed to delete file from disk: "
                + e.getMessage(),
            e);
      }
    } else {
      log.warn(
          "Image ID {} was not found in the property's image collection. No DB or file deletion"
              + " performed for this image.",
          imageId);
      throw new InvalidPropertyImageParameterException(
          "Image ID "
              + imageId
              + " could not be removed from property's collection, it might have already been"
              + " removed.");
    }
  }
}
