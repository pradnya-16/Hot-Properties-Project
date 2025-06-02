package edu.finalproject.hotproperty.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agent")
public class AgentController {

    @GetMapping("/add_properties")
    public String showAddPropertyForm() {
        return "agent/add_properties";
    }
}
