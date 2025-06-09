package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyImageParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.services.AgentMessageService;
import edu.finalproject.hotproperty.services.PropertyService;
import edu.finalproject.hotproperty.services.UserService;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AgentController {

  private static final Logger log = LoggerFactory.getLogger(AgentController.class);

  private final PropertyService propertyService;
  private final UserService userService;
  private final AgentMessageService agentMessageService;

  @Autowired
  public AgentController(
      PropertyService propertyService,
      UserService userService,
      AgentMessageService agentMessageService) {
    this.propertyService = propertyService;
    this.userService = userService;
    this.agentMessageService = agentMessageService;
  }

  @GetMapping("/properties/add")
  @PreAuthorize("hasRole('AGENT')")
  public String showAddPropertyForm() {
    return "agent/add_properties";
  }

  @GetMapping("/properties/manage")
  @PreAuthorize("hasRole('AGENT')")
  public String manageListings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User currentAgent = userService.getCurrentUser(userDetails);
    List<Property> properties = propertyService.getPropertiesByAgentWithImages(currentAgent);
    model.addAttribute("properties", properties);
    return "agent/manage_properties";
  }

  @GetMapping("/properties/edit/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String showEditForm(@PathVariable Long id, Model model) {
    Property property = propertyService.getPropertyWithAgentAndImagesById(id);
    model.addAttribute("property", property);
    return "agent/edit_property";
  }

  @PostMapping("/properties/edit/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String updateProperty(
      @PathVariable Long id,
      @ModelAttribute Property formProperty,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {
    User agent = userService.getCurrentUser(userDetails);
    try {
      propertyService.updateProperty(id, formProperty, agent);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Property details updated successfully!");
    } catch (Exception e) {
      log.error("Error updating property ID {}: {}", id, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Failed to update property: " + e.getMessage());
      return "redirect:/properties/edit/" + id;
    }
    return "redirect:/properties/manage";
  }

  @PostMapping("/properties/add")
  @PreAuthorize("hasRole('AGENT')")
  public String handleAddProperty(
      @RequestParam String title,
      @RequestParam Double price,
      @RequestParam String location,
      @RequestParam Integer size,
      @RequestParam String description,
      @RequestParam(value = "image", required = false) MultipartFile image,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {
    User agent = userService.getCurrentUser(userDetails);
    Property property = new Property();
    property.setTitle(title);
    property.setPrice(price);
    property.setLocation(location);
    property.setSize(size);
    property.setDescription(description);
    try {
      propertyService.addProperty(property, image, agent);
      redirectAttributes.addFlashAttribute("successMessage", "Property added successfully!");
    } catch (IOException e) {
      log.error("Error adding property with title {}: {}", title, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Failed to add property due to image error: " + e.getMessage());
      return "redirect:/properties/add";
    } catch (Exception e) {
      log.error("Error adding property with title {}: {}", title, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Failed to add property: " + e.getMessage());
      return "redirect:/properties/add";
    }
    return "redirect:/properties/manage";
  }

  @PostMapping("/properties/delete/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String deleteProperty(
      @PathVariable("id") Long propertyId,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {
    String agentUsername = userDetails.getUsername();
    log.info("Agent {} attempting to delete property with ID: {}", agentUsername, propertyId);
    try {
      User agent = userService.getCurrentUser(userDetails);
      propertyService.deleteProperty(propertyId, agent);
      log.info("Property with ID {} deleted successfully by agent {}.", propertyId, agentUsername);
      redirectAttributes.addFlashAttribute("successMessage", "Property deleted successfully!");
    } catch (RuntimeException e) {
      log.error(
          "Error deleting property ID {} for agent {}: {}",
          propertyId,
          agentUsername,
          e.getMessage(),
          e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Failed to delete property: " + e.getMessage());
    }
    return "redirect:/properties/manage";
  }

  @GetMapping("/messages/agent")
  @PreAuthorize("hasRole('AGENT')")
  public String viewAllMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User agent = userService.getCurrentUser(userDetails);
    List<edu.finalproject.hotproperty.entities.Message> messages =
        agentMessageService.getMessagesForAgent(agent);
    model.addAttribute("messages", messages);
    return "agent/view_all_messages";
  }

  @GetMapping("/messages/agent/{id}")
  @PreAuthorize("hasRole('AGENT')")
  public String viewMessage(
      @PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
    User agent = userService.getCurrentUser(userDetails);
    edu.finalproject.hotproperty.entities.Message message =
        agentMessageService.getMessageByIdForAgent(id, agent);
    model.addAttribute("message", message);
    return "agent/view_message";
  }

  @PostMapping("/messages/agent/{id}/reply")
  @PreAuthorize("hasRole('AGENT')")
  public String replyToMessage(
      @PathVariable Long id,
      @RequestParam String reply,
      @AuthenticationPrincipal UserDetails userDetails) {
    User agent = userService.getCurrentUser(userDetails);
    agentMessageService.replyToMessage(id, reply, agent);
    return "redirect:/messages/agent";
  }

  @PostMapping("/messages/agent/{id}/delete")
  @PreAuthorize("hasRole('AGENT')")
  public String deleteMessage(
      @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    User agent = userService.getCurrentUser(userDetails);
    agentMessageService.deleteMessageForAgent(id, agent);
    return "redirect:/messages/agent";
  }

  @PostMapping("/properties/edit/{propertyId}/addImage")
  @PreAuthorize("hasRole('AGENT')")
  public String handleAddImageToProperty(
      @PathVariable Long propertyId,
      @RequestParam("imageFile") MultipartFile imageFile,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {
    User agent = userService.getCurrentUser(userDetails);
    try {
      propertyService.addImageToProperty(propertyId, imageFile, agent);
      redirectAttributes.addFlashAttribute("successMessage", "Image added successfully!");
    } catch (IOException e) {
      log.error("Error adding image to property ID {}: {}", propertyId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Failed to add image: " + e.getMessage());
    } catch (InvalidPropertyParameterException
        | InvalidPropertyImageParameterException
        | AccessDeniedException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/properties/edit/" + propertyId;
  }

  @PostMapping("/properties/edit/{propertyId}/removeImage/{imageId}")
  @PreAuthorize("hasRole('AGENT')")
  public String handleRemoveImageFromProperty(
      @PathVariable Long propertyId,
      @PathVariable Long imageId,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {
    User agent = userService.getCurrentUser(userDetails);
    try {
      propertyService.removeImageFromProperty(propertyId, imageId, agent);
      redirectAttributes.addFlashAttribute("successMessage", "Image removed successfully!");
    } catch (IOException e) {
      log.error(
          "Error removing image ID {} from property ID {}: {}",
          imageId,
          propertyId,
          e.getMessage(),
          e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Failed to remove image: " + e.getMessage());
    } catch (InvalidPropertyParameterException
        | InvalidPropertyImageParameterException
        | AccessDeniedException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/properties/edit/" + propertyId;
  }
}
