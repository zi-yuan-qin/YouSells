package com.yousells.modules.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoginAndReadCurrentUser() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.userInfo.username").value("admin"))
                .andReturn();

        String accessToken = JsonTestUtils.readJsonPath(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.displayName").value("系统管理员"));
    }

    @Test
    void shouldRejectCurrentUserWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4003));
    }

    @Test
    void shouldRejectMalformedBearerHeader() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = JsonTestUtils.readJsonPath(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer" + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4003));
    }
}
