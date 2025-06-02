package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.*;
import edu.finalproject.hotproperty.repositories.*;

import edu.finalproject.hotproperty.services.AuthServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BuyerController {
  //for when we start implementing stuff in here and need logging
  private static final Logger log = LoggerFactory.getLogger(BuyerController.class); 
  
  private final UserRepository userRepository;
  private final PropertyRepository propertyRepository;
  private final MessageRepository messageRepository;
  private final FavoriteRepository favoriteRepository;

  @Autowired
  public BuyerController(UserRepository userRepository, PropertyRepository propertyRepository,
      PropertyImageRepository propertyImageRepository, MessageRepository messageRepository,
      FavoriteRepository favoriteRepository) {
    this.userRepository = userRepository;
    this.propertyRepository = propertyRepository;
    this.messageRepository = messageRepository;
    this.favoriteRepository = favoriteRepository;
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/favorites")
  public String viewFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User buyer = userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
    List<Favorite> favorites = favoriteRepository.findByBuyer(buyer);
    model.addAttribute("favorites", favorites);
    return "/buyer/favorites";
  }

  @PreAuthorize("hasRole('BUYER')")
  @GetMapping("/messages/buyer")
  public String viewMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
      User buyer = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
      List<Message> messages = messageRepository.findBySender(buyer);
      model.addAttribute("messages", messages);
      return "/buyer/view_messages";
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

      // Just for now, find all (later filter based on params)
      List<Property> properties = propertyRepository.findAll();

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
  public String viewPropertyDetail(@PathVariable Long id, Model model) {
      Property property = propertyRepository.findWithImagesById(id).orElseThrow();
      model.addAttribute("property", property);
      return "/buyer/property_details_view";
  }
}
