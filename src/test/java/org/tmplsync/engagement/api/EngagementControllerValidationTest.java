package org.tmplsync.engagement.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tmplsync.engagement.service.EngagementService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EngagementController.class)
class EngagementControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EngagementService engagementService;

    @Test
    void returnsBadRequestWhenRequiredFieldsAreNull() throws Exception {
        UUID engagementId = UUID.randomUUID();

        mockMvc.perform(post("/internal/v1/engagements/{engagementId}", engagementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firmId\":null,\"templateId\":null,\"appliedVersion\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field=='firmId')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='templateId')]").exists());
    }

    @Test
    void returnsBadRequestWhenDecisionIsNull() throws Exception {
        UUID engagementId = UUID.randomUUID();

        mockMvc.perform(post("/internal/v1/engagements/{engagementId}/decision", engagementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("decision"));
    }
}
