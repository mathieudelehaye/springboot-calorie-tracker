package org.example.controller;

// Database info service removed (popup functionality disabled)
// import org.example.service.DatabaseInfoService;
// Debug endpoint dependencies removed
// import org.example.repository.CoachRepository;
// import org.example.model.Coach;
// Unused import removed (no autowired fields)
// // Unused imports removed (no autowired fields or API endpoints)
// import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;

// Unused import removed (database info endpoint disabled)
// import java.util.Map;
// Unused imports removed (debug endpoint disabled)
// import java.util.List;
// import java.util.stream.Collectors;

@Controller
public class AdminLoginController {

    // Database info service removed (popup functionality disabled)
    // @Autowired
    // private DatabaseInfoService databaseInfoService;

    // Debug endpoint repository removed (security)
    // @Autowired
    // private CoachRepository coachRepository;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        // Add demo credentials for display
        model.addAttribute("demoUsername", "Mathieu");
        model.addAttribute("demoPassword", "Mathieu");
        
        return "login";
    }

    // Database info endpoint disabled (popup removed from UI)
    // @GetMapping("/api/database-info")
    // @ResponseBody
    // public ResponseEntity<Map<String, Object>> getDatabaseInfo() { ... }

    // Debug endpoint DTO class removed (security)
    // public static class CoachInfo { ... }

    // Debug endpoint disabled for security
    // @GetMapping("/api/debug/coaches")
} 