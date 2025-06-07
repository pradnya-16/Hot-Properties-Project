package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Favorite;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidFavoriteParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.repositories.UserRepository;
import edu.finalproject.hotproperty.services.BuyerService;
import edu.finalproject.hotproperty.services.FavoriteService;
import edu.finalproject.hotproperty.services.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.services.BuyerMessageService;
import edu.finalproject.hotproperty.exceptions.InvalidMessageParameterException;

@Controller
public class BuyerController {

  private static final Logger log = LoggerFactory.getLogger(BuyerController.class);

  private final BuyerService buyerService;
  private final PropertyService propertyService;
  private final UserRepository userRepository;
  private final FavoriteService favoriteService;
  private final BuyerMessageService buyerMessageService;
  @Autowired
  public BuyerController(
      BuyerService buyerService,
      PropertyService propertyService,
      UserRepository userRepository,
      FavoriteService favoriteService,
      BuyerMessageService buyerMessageService) {
    this.buyerService = buyerService;
    this.propertyService = propertyService;
    this.userRepository = userRepository;
    this.favoriteService = favoriteService;
    this.buyerMessageService = buyerMessageService;
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/favorites")
  public String viewFavoritesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User buyer =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(
                () ->
                    new UsernameNotFoundException("Buyer not found: " + userDetails.getUsername()));
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
    User buyer =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(
                () ->
                    new UsernameNotFoundException("Buyer not found: " + userDetails.getUsername()));
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
    // redirect to previous page or property view
    return "redirect:" + (referer != null ? referer : "/properties/view/" + propertyId);
  }

  @PreAuthorize("hasRole('BUYER')")
  @PostMapping("/favorites/remove/{propertyId}")
  public String removePropertyFromFavorites(
      @PathVariable Long propertyId,
      @AuthenticationPrincipal UserDetails userDetails,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    User buyer =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(
                () ->
                    new UsernameNotFoundException("Buyer not found: " + userDetails.getUsername()));
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
    // if referer from favorites page, redirect to favorites page
    if (referer != null && referer.contains("/favorites")) {
      return "redirect:/favorites";
    }
    return "redirect:" + (referer != null ? referer : "/properties/view/" + propertyId);
  }



  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/messages/send/{propertyId}")
  public String showSendMessageForm(@PathVariable Long propertyId, Model model) {
    model.addAttribute("propertyId", propertyId);
    return "buyer/send_message";
  }

  @PreAuthorize("hasRole('BUYER')")
  @PostMapping("/messages/send/{propertyId}")
  public String sendMessage(
          @PathVariable Long propertyId,
          @RequestParam("content") String content,
          @AuthenticationPrincipal UserDetails userDetails,
          RedirectAttributes redirectAttributes) {

    User buyer = buyerService.findByEmail(userDetails.getUsername());

    try {
      buyerMessageService.sendMessage(buyer, propertyId, content);
      redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully.");
    } catch (InvalidMessageParameterException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error sending message for property {}: {}", propertyId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while sending message.");
    }
    return "redirect:/properties/view/" + propertyId;
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/messages/inbox")
  public String viewBuyerMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User buyer = buyerService.findByEmail(userDetails.getUsername());
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

    User buyer = buyerService.findByEmail(userDetails.getUsername());
    try {
      buyerMessageService.deleteMessage(buyer, messageId);
      redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully.");
    } catch (InvalidMessageParameterException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error deleting message {}: {}", messageId, e.getMessage(), e);
      redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting message.");
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

    if (userDetails != null) {
      User buyer = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
      if (buyer != null) {
        boolean isFavorite = favoriteService.isPropertyFavorite(buyer, id);
        model.addAttribute("isFavorite", isFavorite);
      } else {
        log.warn("Authenticated userDetails present but no matching buyer found: {}", userDetails.getUsername());
        model.addAttribute("isFavorite", false);
      }
    } else {
      log.warn("No authenticated userDetails found when accessing property details for property ID: {}", id);
      model.addAttribute("isFavorite", false);
    }
    return "/buyer/property_details_view";
  }
}

















