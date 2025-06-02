package edu.finalproject.hotproperty.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AgentController {

    @GetMapping("/properties/add")
    public String showAddPropertyForm() {
        return "agent/add_properties";
    }

}
