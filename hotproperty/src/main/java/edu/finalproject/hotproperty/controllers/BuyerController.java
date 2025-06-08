package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.*;
import edu.finalproject.hotproperty.exceptions.InvalidFavoriteParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidMessageParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.services.BuyerMessageService;
import edu.finalproject.hotproperty.services.BuyerService;
import edu.finalproject.hotproperty.services.FavoriteService;
import edu.finalproject.hotproperty.services.PropertyService;
import edu.finalproject.hotproperty.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BuyerController {

  private static final Logger log = LoggerFactory.getLogger(BuyerController.class);

  private final BuyerService buyerService;
  private final PropertyService propertyService;
  private final FavoriteService favoriteService;
  private final BuyerMessageService buyerMessageService;
  private final UserService userService;

  @Autowired
  public BuyerController(
      BuyerService buyerService,
      PropertyService propertyService,
      FavoriteService favoriteService,
      BuyerMessageService buyerMessageService,
      UserService userService) {
    this.buyerService = buyerService;
    this.propertyService = propertyService;
    this.favoriteService = favoriteService;
    this.buyerMessageService = buyerMessageService;
    this.userService = userService;
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/favorites")
  public String viewFavoritesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User buyer = userService.getCurrentUser(userDetails);
    List<Favorite> favorites = favoriteService.getFavoritesByBuyer(buyer);
    model.addAttribute("favorites", favorites);
    return "buyer/favorites";
  }

  @PreAuthorize("hasRole('BUYER')")
  @PostMapping("/favorites/add/{propertyId}")
  public String addPropertyToFavorites(
      @PathVariable Long propertyId,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    User buyer = userService.getCurrentUser(userDetails);
    try {
      favoriteService.addFavorite(buyer, propertyId);
      redirectAttributes.addFlashAttribute("successMessage", "Property added to favorites!");
    } catch (InvalidPropertyParameterException | InvalidFavoriteParameterException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error adding favorite for property ID {}: {}", propertyId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An unexpected error occurred while adding favorite.");
    }
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/properties/view/" + propertyId);
  }

  @PreAuthorize("hasRole('BUYER')")
  @PostMapping("/favorites/remove/{propertyId}")
  public String removePropertyFromFavorites(
      @PathVariable Long propertyId,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    User buyer = userService.getCurrentUser(userDetails);
    try {
      favoriteService.removeFavorite(buyer, propertyId);
      redirectAttributes.addFlashAttribute("successMessage", "Property removed from favorites.");
    } catch (InvalidPropertyParameterException | InvalidFavoriteParameterException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error removing favorite for property ID {}: {}", propertyId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An unexpected error occurred while removing favorite.");
    }
    String referer = request.getHeader("Referer");
    if (referer != null && referer.contains("/favorites")) {
      return "redirect:/favorites";
    }
    return "redirect:" + (referer != null ? referer : "/properties/view/" + propertyId);
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/messages/send/{propertyId}")
  public String showSendMessageForm(@PathVariable Long propertyId, Model model) {
    model.addAttribute("propertyId", propertyId);
    Property property = propertyService.findWithImagesById(propertyId);
    model.addAttribute("propertyTitle", property.getTitle());
    return "buyer/send_message";
  }

  @PreAuthorize("hasRole('BUYER')")
  @PostMapping("/messages/send/{propertyId}")
  public String sendMessage(
      @PathVariable Long propertyId,
      @RequestParam("content") String content,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    User buyer = userService.getCurrentUser(userDetails);

    try {
      buyerMessageService.sendMessage(buyer, propertyId, content);
      redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully.");
    } catch (InvalidMessageParameterException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error sending message for property {}: {}", propertyId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An error occurred while sending message.");
    }
    return "redirect:/properties/view/" + propertyId;
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/messages/inbox")
  public String viewBuyerMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User buyer = userService.getCurrentUser(userDetails);
    List<Message> messages = buyerMessageService.getBuyerMessages(buyer);
    model.addAttribute("messages", messages);
    return "buyer/buyer_messages";
  }

  @PreAuthorize("hasRole('BUYER')")
  @PostMapping("/messages/delete/{messageId}")
  public String deleteMessage(
      @PathVariable Long messageId,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    User buyer = userService.getCurrentUser(userDetails);
    try {
      buyerMessageService.deleteMessage(buyer, messageId);
      redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully.");
    } catch (InvalidMessageParameterException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error deleting message {}: {}", messageId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An error occurred while deleting message.");
    }
    return "redirect:/messages/inbox";
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/properties/list")
  public String viewProperties(
      @RequestParam(required = false) String zip,
      @RequestParam(required = false) Integer minSqFt,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(required = false, defaultValue = "asc") String sortBy,
      Model model) {

    List<Property> properties =
        propertyService.filterProperties(zip, minSqFt, minPrice, maxPrice, sortBy);

    model.addAttribute("properties", properties);
    model.addAttribute("zip", zip);
    model.addAttribute("minSqFt", minSqFt);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("sortBy", sortBy);

    return "/buyer/browse_properties";
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/properties/view/{id}")
  public String viewPropertyDetail(
      @PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
    Property property = propertyService.findWithImagesById(id);
    model.addAttribute("property", property);

    if (property.getImages() != null) {
      List<String> imageFileNames =
          property.getImages().stream()
              .map(PropertyImage::getImageFileName)
              .collect(Collectors.toList());
      model.addAttribute("imageFileNames", imageFileNames);
    }

    if (userDetails != null) {
      User buyer = userService.getCurrentUser(userDetails);
      boolean isFavorite = favoriteService.isPropertyFavorite(buyer, id);
      model.addAttribute("isFavorite", isFavorite);
    } else {
      log.warn(
          "No authenticated userDetails found when accessing property details for property ID: {}",
          id);
      model.addAttribute("isFavorite", false);
    }
    return "/buyer/property_details_view";
  }
}
