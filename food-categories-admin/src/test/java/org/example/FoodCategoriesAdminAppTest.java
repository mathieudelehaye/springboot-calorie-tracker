package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
@ActiveProfiles("test")
class FoodCategoriesAdminAppTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
        Authentication auth = new UsernamePasswordAuthenticationToken("Mathieu", "Mathieu");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
} 