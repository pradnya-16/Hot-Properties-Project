package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Favorite;
import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BuyerController {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final MessageRepository messageRepository;
    private final FavoriteRepository favoriteRepository;

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
        return "/favorites";
    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/messages/buyer")
    public String viewMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Message> messages = messageRepository.findBySender(buyer);
        model.addAttribute("messages", messages);
        return "buyer/messages";
    }
}
