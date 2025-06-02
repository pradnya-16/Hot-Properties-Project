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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BuyerController {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final MessageRepository messageRepository;
    private final FavoriteRepository favoriteRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    public BuyerController(UserRepository userRepository, PropertyRepository propertyRepository, PropertyImageRepository propertyImageRepository, MessageRepository messageRepository, FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.messageRepository = messageRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    public String buyerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "/shared/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String buyerProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        return "/shared/profile";
    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/favorites")
    public String viewFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
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
