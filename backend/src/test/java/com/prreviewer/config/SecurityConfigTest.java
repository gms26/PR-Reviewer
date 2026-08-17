package com.prreviewer.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetHealthIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
               .andExpect(status().isOk());
    }

    @Test
    void testPostHealthIsUnauthorized() throws Exception {
        mockMvc.perform(post("/health"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedApiRequiresAuthentication() throws Exception {
        // Assuming /api/some-endpoint is a protected endpoint, but we can just test any random one since anyRequest().authenticated() is used.
        mockMvc.perform(get("/api/repositories"))
               .andExpect(status().isUnauthorized());
    }
}
