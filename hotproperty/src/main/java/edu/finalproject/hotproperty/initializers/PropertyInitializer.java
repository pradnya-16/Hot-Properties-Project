package edu.finalproject.hotproperty.initializers;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.PropertyImage;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.repositories.PropertyImageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@DependsOn("userInitializer")
public class PropertyInitializer {
  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final UserRepository userRepository;

  private static final Logger log = LoggerFactory.getLogger(PropertyInitializer.class);

  private static final String RUNTIME_IMAGES_BASE_PATH = "uploads/properties/";
  private static final String SAMPLE_IMAGES_SOURCE_ROOT_IN_PROJECT =
      "src/main/resources/static/PropertyImages/";

  @Autowired
  public PropertyInitializer(
      PropertyRepository propertyRepository,
      UserRepository userRepository,
      PropertyImageRepository propertyImageRepository) {
    this.propertyRepository = propertyRepository;
    this.userRepository = userRepository;
    this.propertyImageRepository = propertyImageRepository;
  }

  private String getBaseName(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return "image";
    }
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot > 0) {
      return fileName.substring(0, lastDot);
    }
    return fileName;
  }

  private String getExtension(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return ".jpg";
    }
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot >= 0 && lastDot < fileName.length() - 1) {
      return fileName.substring(lastDot);
    }
    return "";
  }

  private String generateUniqueFilenameWithUUID(String originalFilename) {
    String cleanedOriginalFilename = StringUtils.cleanPath(originalFilename);
    String baseName = getBaseName(cleanedOriginalFilename);
    String extension = getExtension(cleanedOriginalFilename);
    String uuid = UUID.randomUUID().toString();
    return baseName + "_" + uuid + extension;
  }

  @PostConstruct
  @Transactional
  public void init() {
    if (propertyRepository.count() == 0) {
      User agent1 =
          userRepository
              .findByEmail("agent1@example.com")
              .orElseThrow(
                  () -> new RuntimeException("Agent1 not found for property initialization"));
      User agent2 =
          userRepository
              .findByEmail("agent2@example.com")
              .orElseThrow(
                  () -> new RuntimeException("Agent2 not found for property initialization"));

      Path runtimeImageDir = Paths.get(RUNTIME_IMAGES_BASE_PATH);
      try {
        if (!Files.exists(runtimeImageDir)) {
          Files.createDirectories(runtimeImageDir);
          log.info("Created runtime image directory: {}", runtimeImageDir.toAbsolutePath());
        }
      } catch (IOException e) {
        log.error(
            "Could not create runtime image directory {}: {}",
            runtimeImageDir.toAbsolutePath(),
            e.getMessage());
        throw new RuntimeException("Failed to create runtime image directory", e);
      }

      List<Property> propertiesToCreate =
          List.of(
              new Property(
                  "3818 N Christiana Ave",
                  1025000.0,
                  "Chicago, IL 60618",
                  3600,
                  "Experience luxury living...",
                  agent1),
              new Property(
                  "3423 N Kedzie Ave",
                  899000.0,
                  "Chicago, IL 60618",
                  4600,
                  "Oversized all-brick single-family home...",
                  agent1),
              new Property(
                  "1837 N Fremont St",
                  3795000.0,
                  "Chicago, IL 60614",
                  4662,
                  "Welcome to this architectural masterpiece...",
                  agent1),
              new Property(
                  "2818 W Wellington Ave",
                  899000.0,
                  "Chicago, IL 60618",
                  3000,
                  "Experience Unparalleled Luxury!...",
                  agent1),
              new Property(
                  "3454 W Potomac Ave",
                  959000.0,
                  "Chicago, IL 60651",
                  4098,
                  "Modern 6 bed, 4.1 bath new construction...",
                  agent1),
              new Property(
                  "461 W Melrose St",
                  3300000.0,
                  "Chicago, IL 60657",
                  5400,
                  "East Lakeview is the setting...",
                  agent1),
              new Property(
                  "1741 N Mozart St",
                  849000.0,
                  "Chicago, IL 60647",
                  2631,
                  "Reimagined Logan Square single-family home...",
                  agent1),
              new Property(
                  "2317 W Ohio St",
                  949000.0,
                  "Chicago, IL 60612",
                  3000,
                  "Fully gut-rehabbed in 2022...",
                  agent1),
              new Property(
                  "1701 N Dayton St",
                  4750000.0,
                  "Chicago, IL 60614",
                  8000,
                  "Stunning LG custom-built single-family home...",
                  agent1),
              new Property(
                  "334 N Jefferson St UNIT D",
                  925000.0,
                  "Chicago, IL 60661",
                  2600,
                  "This rare corner townhome in Kinzie Station...",
                  agent1),
              new Property(
                  "1249 S Plymouth Ct",
                  1200000.0,
                  "Chicago, IL 60605",
                  3000,
                  "Welcome home to your urban oasis...",
                  agent2),
              new Property(
                  "2779 N Kenmore Ave",
                  1300000.0,
                  "Chicago, IL 60614",
                  2532,
                  "Unicorn alert! This rare, beautifully renovated Victorian...",
                  agent2),
              new Property(
                  "4425 N Winchester Ave",
                  1125000.0,
                  "Chicago, IL 60640",
                  3000,
                  "Lovely Victorian home on large lot...",
                  agent2),
              new Property(
                  "4511 N Saint Louis Ave",
                  889000.0,
                  "Chicago, IL 60625",
                  3213,
                  "A perfect blend of traditional charm...",
                  agent2),
              new Property(
                  "401 W Dickens Ave",
                  5995000.0,
                  "Chicago, IL 60614",
                  7252,
                  "This ultra notable single-family home...",
                  agent2),
              new Property(
                  "339 W Webster Ave UNIT 2B",
                  1225000.0,
                  "Chicago, IL 60614",
                  2400,
                  "Enjoy the perfect, prime East Lincoln Park location...",
                  agent2),
              new Property(
                  "1541 W Addison St",
                  1200000.0,
                  "Chicago, IL 60613",
                  2869,
                  "Don't miss this pristine, beautifully rehabbed...",
                  agent2));

      for (Property property : propertiesToCreate) {
        Property savedProperty = propertyRepository.save(property);
        log.info("Saved property: {}", savedProperty.getTitle());

        Path sourcePropertyImageFolder =
            Paths.get(SAMPLE_IMAGES_SOURCE_ROOT_IN_PROJECT, property.getTitle());

        if (Files.exists(sourcePropertyImageFolder)
            && Files.isDirectory(sourcePropertyImageFolder)) {
          try (Stream<Path> imageFilesStream = Files.list(sourcePropertyImageFolder)) {
            imageFilesStream
                .filter(Files::isRegularFile)
                .forEach(
                    sourceImagePath -> {
                      String originalFileName = sourceImagePath.getFileName().toString();
                      String uniqueDbFilename = generateUniqueFilenameWithUUID(originalFileName);
                      Path destinationPath = runtimeImageDir.resolve(uniqueDbFilename);
                      try {
                        Files.copy(
                            sourceImagePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        PropertyImage propertyImage =
                            new PropertyImage(uniqueDbFilename, savedProperty);
                        propertyImageRepository.save(propertyImage);
                        log.info(
                            "Copied sample image {} to {} for property {}",
                            originalFileName,
                            destinationPath,
                            savedProperty.getTitle());
                      } catch (IOException e) {
                        log.error(
                            "Failed to copy sample image {} for property {}: {}",
                            originalFileName,
                            savedProperty.getTitle(),
                            e.getMessage());
                      }
                    });
          } catch (IOException e) {
            log.error(
                "Could not list images for property {}: {}", property.getTitle(), e.getMessage());
          }
        } else {
          log.warn(
              "Sample image folder not found for property {}: {}",
              savedProperty.getTitle(),
              sourcePropertyImageFolder.toString());
        }
      }
      log.info("Sample properties and images initialization attempt complete.");
    } else {
      log.info("Properties already exist, skipping initialization.");
    }
  }
}
