package com.compass.digitalbank.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.seed.enabled=false",
        "app.rate-limit.enabled=false",
        "app.messaging.enabled=false"
})
class ApiEndToEndIT {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supportsFullTransferJourneyOverHttp() throws Exception {
        String token = register("Carol", "carol@example.com", "Password123!");

        mockMvc.perform(get(ApiPaths.ACCOUNTS))
                .andExpect(status().isUnauthorized());

        String sourceId = createAccount(token, "Checking", "1000.00");
        String destinationId = createAccount(token, "Savings", "0.00");

        mockMvc.perform(post(ApiPaths.TRANSFERS)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "sourceAccountId", sourceId,
                                "destinationAccountId", destinationId,
                                "amount", "250.00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get(ApiPaths.ACCOUNTS + "/{id}/transactions", sourceId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].direction").value("DEBIT"));
    }

    @Test
    void rejectsInvalidTransferPayload() throws Exception {
        String token = register("Dave", "dave@example.com", "Password123!");
        String sourceId = createAccount(token, "Checking", "100.00");
        String destinationId = createAccount(token, "Savings", "0.00");

        mockMvc.perform(post(ApiPaths.TRANSFERS)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "sourceAccountId", sourceId,
                                "destinationAccountId", destinationId,
                                "amount", "-5.00"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsWeakPassword() throws Exception {
        mockMvc.perform(post(ApiPaths.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Eve", "email", "eve@example.com", "password", "onlyletters"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsMalformedPathVariable() throws Exception {
        String token = register("Frank", "frank@example.com", "Password123!");

        mockMvc.perform(get(ApiPaths.ACCOUNTS + "/{id}/transactions", "not-a-uuid")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void normalizesEmailCaseBetweenRegisterAndLogin() throws Exception {
        register("Grace", "Grace@Example.com", "Password123!");

        mockMvc.perform(post(ApiPaths.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "grace@example.com", "password", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    private String register(String name, String email, String password) throws Exception {
        String response = mockMvc.perform(post(ApiPaths.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name, "email", email, "password", password))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    private String createAccount(String token, String name, String initialBalance) throws Exception {
        String response = mockMvc.perform(post(ApiPaths.ACCOUNTS)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name, "initialBalance", initialBalance))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Map<String, String> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }
}
