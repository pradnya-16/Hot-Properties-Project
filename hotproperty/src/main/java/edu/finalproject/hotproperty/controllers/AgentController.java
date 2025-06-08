package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.PropertyImage;
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
        .orElseThrow(() -> new RuntimeException("Agent user not found: " + userDetails.getUsername()));
    List<Property> properties = propertyRepository.findWithImagesByAgent(currentAgent);

    model.addAttribute("properties", properties);
    return "agent/manage_properties";
  }

  @GetMapping("/properties/edit/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String showEditForm(@PathVariable Long id, Model model) {
    Property property = propertyRepository.findWithAgentAndImagesById(id)
        .orElseThrow(() -> new RuntimeException("Property not found"));

    model.addAttribute("property", property);
    return "agent/edit_property";
  }

  @PostMapping("/properties/edit/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String updateProperty(@PathVariable Long id,
      @ModelAttribute Property formProperty) {
    Property property = propertyRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Property not found"));

    property.setTitle(formProperty.getTitle());
    property.setLocation(formProperty.getLocation());
    property.setPrice(formProperty.getPrice());
    property.setSize(formProperty.getSize());
    property.setDescription(formProperty.getDescription());

    propertyRepository.save(property);
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
        .orElseThrow(() -> new RuntimeException("Agent not found"));

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

  @PostMapping("/properties/manage/delete/{propertyId}")
  @PreAuthorize("hasRole('AGENT')")
  public String deleteProperty(@PathVariable Long propertyId,
      @AuthenticationPrincipal UserDetails userDetails
      ) {
    String agentUsername = userDetails.getUsername();
    log.info("Agent {} attempting to delete property with ID: {}", agentUsername, propertyId);
    try {
      var agent = userRepository.findByEmail(agentUsername)
          .orElseThrow(() -> {
            log.error("Agent user '{}' not found in database during delete operation for property ID {}.",
                agentUsername, propertyId);
            return new UsernameNotFoundException("Agent not found: " + agentUsername);
          });
      propertyService.deleteProperty(propertyId, agent);
      log.info("Property with ID {} deleted successfully by agent {}.", propertyId, agentUsername);
    } catch (RuntimeException e) {
      log.error("Error deleting property ID {} for agent {}: {}", propertyId, agentUsername, e.getMessage(), e);
    }
    return "redirect:/properties/manage";

  }

  @GetMapping("/messages/agent")
  @PreAuthorize("hasRole('AGENT')")
  public String viewAllMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    var agent = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Agent not found"));
    var messages = messageRepository.findByProperty_Agent(agent);
    model.addAttribute("messages", messages);
    return "agent/view_all_messages";
  }


  @GetMapping("/messages/agent/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String viewMessage(@PathVariable Long id, Model model) {
    var message = messageRepository.findWithSenderAndPropertyById(id)
            .orElseThrow(() -> new RuntimeException("Message not found"));

    model.addAttribute("message", message);
    return "agent/view_message";
  }


  @PostMapping("/messages/agent/{id}/reply")
  @PreAuthorize("hasRole('AGENT')")
  public String replyToMessage(@PathVariable Long id,
                               @RequestParam String reply) {
    var message = messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found"));
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
