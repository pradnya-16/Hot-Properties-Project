package edu.finalproject.hotproperty.controllers;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.PropertyImage;
import edu.finalproject.hotproperty.repositories.PropertyImageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class AgentController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyImageRepository propertyImageRepository;

    @Autowired
    public AgentController(PropertyRepository propertyRepository, UserRepository userRepository, PropertyImageRepository propertyImageRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    @GetMapping("/properties/add")
    @PreAuthorize("hasRole('AGENT')")
    public String showAddPropertyForm() {
        return "agent/add_properties";
    }

    @GetMapping("/properties/manage")
    @PreAuthorize("hasRole('AGENT')")
    public String manageListings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            throw new RuntimeException("UserDetails is null — are you logged in?");
        }

        List<Property> properties = propertyRepository.findAll();
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
            if (!dir.exists()) dir.mkdirs();

            Path imagePath = Paths.get(folder, image.getOriginalFilename());
            Files.write(imagePath, image.getBytes());

            PropertyImage propertyImage = new PropertyImage(image.getOriginalFilename(), property);
            propertyImageRepository.save(propertyImage);
        }

        return "redirect:/properties/manage";
    }


}
