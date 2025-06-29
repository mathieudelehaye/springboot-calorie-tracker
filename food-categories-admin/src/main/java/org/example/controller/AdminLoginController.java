package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminLoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        
        // Add demo credentials for display
        model.addAttribute("demoUsername", "mathieu");
        model.addAttribute("demoPassword", "coach123");
        
        return "login";
    }
} 