package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.PropertyImage;
import edu.finalproject.hotproperty.exceptions.InvalidMessageParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidUserParameterException;
import edu.finalproject.hotproperty.repositories.MessageRepository;
import edu.finalproject.hotproperty.repositories.PropertyImageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import edu.finalproject.hotproperty.services.PropertyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class AgentController {

  private static final Logger log = LoggerFactory.getLogger(AgentController.class);
  private final PropertyRepository propertyRepository;
  private final UserRepository userRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final PropertyService propertyService;
  private final MessageRepository messageRepository;

  @Autowired
  public AgentController(PropertyRepository propertyRepository, UserRepository userRepository,
      PropertyImageRepository propertyImageRepository, PropertyService propertyService, MessageRepository messageRepository) {
    this.propertyRepository = propertyRepository;
    this.userRepository = userRepository;
    this.propertyImageRepository = propertyImageRepository;
    this.propertyService = propertyService;
    this.messageRepository = messageRepository;
  }

  @GetMapping("/properties/add")
  @PreAuthorize("hasRole('AGENT')")
  public String showAddPropertyForm() {
    return "agent/add_properties";
  }

  @GetMapping("/properties/manage")
  @PreAuthorize("hasRole('AGENT')")
  public String manageListings(@AuthenticationPrincipal UserDetails userDetails, Model model) {

    var currentAgent = userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(
            () -> {
              log.warn(
                      "Attempt to manage properties with agent name: {}",
                      userDetails.getUsername());
              return new InvalidUserParameterException(
                      "User not found with name: " + userDetails.getUsername());
            });
    List<Property> properties = propertyRepository.findWithImagesByAgent(currentAgent);

    model.addAttribute("properties", properties);
    return "agent/manage_properties";
  }

  @GetMapping("/properties/edit/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String showEditForm(@PathVariable Long id, Model model) {
    Property property = propertyRepository.findWithAgentAndImagesById(id)
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to edit property with id: {}",
                              id);
                      return new InvalidPropertyParameterException(
                              "Property not found with id: " + id);
                    });

    model.addAttribute("property", property);
    return "agent/edit_property";
  }

  @PostMapping("/properties/edit/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String updateProperty(@PathVariable Long id,
                               @ModelAttribute Property formProperty) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to update property with id: {}",
                              id);
                      return new InvalidPropertyParameterException(
                              "Property not found with id: " + id);
                    });

    String oldTitle = property.getTitle();
    String newTitle = formProperty.getTitle();

    if (!oldTitle.equals(newTitle)) {
      boolean exists = propertyRepository.existsByTitle(newTitle);
      if (exists) {
        log.warn("Property title already exists.");
        throw new InvalidPropertyParameterException("Property title already exists.");
      }

      Path oldFolder = Paths.get("src/main/resources/static/PropertyImages", oldTitle);
      Path newFolder = Paths.get("src/main/resources/static/PropertyImages", newTitle);
      try {
        if (Files.exists(oldFolder)) {
          Files.move(oldFolder, newFolder, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException e) {
        e.printStackTrace();
        log.warn("Failed to rename image folder.");
        throw new InvalidPropertyParameterException("Failed to rename image folder.");
      }
    }

    property.setTitle(newTitle);
    property.setLocation(formProperty.getLocation());
    property.setPrice(formProperty.getPrice());
    property.setSize(formProperty.getSize());
    property.setDescription(formProperty.getDescription());

    propertyRepository.save(property);
    log.info("Property updated successfully.");
    return "redirect:/properties/manage";
  }

  @PostMapping("/properties/add")
  @PreAuthorize("hasRole('AGENT')")
  public String handleAddProperty(@RequestParam String title,
      @RequestParam Double price,
      @RequestParam String location,
      @RequestParam Integer size,
      @RequestParam String description,
      @RequestParam(value = "image", required = false) MultipartFile image,
      @AuthenticationPrincipal UserDetails userDetails) throws IOException {

    var agent = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to add properties with agent name: {}",
                              userDetails.getUsername());
                      return new InvalidUserParameterException(
                              "User not found with name: " + userDetails.getUsername());
                    });

    Property property = new Property();
    property.setTitle(title);
    property.setPrice(price);
    property.setLocation(location);
    property.setSize(size);
    property.setDescription(description);
    property.setAgent(agent);

    propertyRepository.save(property);

    if (image != null && !image.isEmpty()) {
      String folder = "src/main/resources/static/PropertyImages/" + title;
      File dir = new File(folder);
      if (!dir.exists())
        dir.mkdirs();

      Path imagePath = Paths.get(folder, image.getOriginalFilename());
      Files.write(imagePath, image.getBytes());

      PropertyImage propertyImage = new PropertyImage(image.getOriginalFilename(), property);
      propertyImageRepository.save(propertyImage);
    }

    return "redirect:/properties/manage";
  }

  @PostMapping("/properties/delete/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String deleteProperty(@PathVariable Long id) {
    Property property = propertyRepository.findById(id)
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to delete property with id: {}",
                              id);
                      return new InvalidPropertyParameterException(
                              "Property not found with id: " + id);
                    });

    String title = property.getTitle();
    Path folderPath = Paths.get("src/main/resources/static/PropertyImages", title);

    try {
      if (Files.exists(folderPath)) {
        Files.walk(folderPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
      }
    } catch (IOException e) {
      e.printStackTrace();
      log.warn("Failed to delete property image folder.");
      return "redirect:/properties/manage";
    }

    propertyRepository.delete(property);
    log.warn("Property deleted successfully.");
    return "redirect:/properties/manage";
  }

  @GetMapping("/messages/agent")
  @PreAuthorize("hasRole('AGENT')")
  public String viewAllMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    var agent = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to view message with agent name: {}",
                              userDetails.getUsername());
                      return new InvalidUserParameterException(
                              "User not found with name: " + userDetails.getUsername());
                    });
    var messages = messageRepository.findByProperty_Agent(agent);
    model.addAttribute("messages", messages);
    return "agent/view_all_messages";
  }


  @GetMapping("/messages/agent/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String viewMessage(@PathVariable Long id, Model model) {
    var message = messageRepository.findWithSenderAndPropertyById(id)
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to view message with id: {}",
                              id);
                      return new InvalidMessageParameterException(
                              "Message not found with id: " + id);
                    });

    model.addAttribute("message", message);
    return "agent/view_message";
  }


  @PostMapping("/messages/agent/{id}/reply")
  @PreAuthorize("hasRole('AGENT')")
  public String replyToMessage(@PathVariable Long id, @RequestParam String reply) {
    var message = messageRepository.findById(id)
            .orElseThrow(
                    () -> {
                      log.warn(
                              "Attempt to reply message with id: {}",
                              id);
                      return new InvalidMessageParameterException(
                              "Message not found with id: " + id);
                    });
    message.setReply(reply);
    messageRepository.save(message);
    return "redirect:/messages/agent";
  }

  @PostMapping("/messages/agent/{id}/delete")
  @PreAuthorize("hasRole('AGENT')")
  public String deleteMessage(@PathVariable Long id) {
    messageRepository.deleteById(id);
    return "redirect:/messages/agent";
  }
}
