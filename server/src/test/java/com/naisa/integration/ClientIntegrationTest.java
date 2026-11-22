package com.naisa.integration;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests API key issuance through the client endpoint.
 *
 * @author James Chen, Codex
 */
@Order(50)
class ClientIntegrationTest extends IntegrationTest {
    private String getApiKey() throws Exception {
        final MvcResult issueResult = mvc.perform(MockMvcRequestBuilders.post("/v1/clients"))
            .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        final String content = issueResult.getResponse().getContentAsString();
        return jsonMapper.readTree(content).path("data").path("apiKey").asString();
    }

    @Test
    void issuedApiKeyNotEmpty() throws Exception {
        final String apiKey = getApiKey();
        assertEquals(36, apiKey.length());
    }

    /**
     * Verifies that a client can retrieve a seeded venue using its newly issued API key.
     * 
     * @author James Chen, Codex
     */
    @Test
    void issuedApiKeyAuthorizesProtectedRequest() throws Exception {
        final String apiKey = getApiKey();
        mvc.perform(
            MockMvcRequestBuilders.get("/v1/venues/1").header("Authorization", "Bearer " + apiKey)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Verifies that two calls to {@code POST /v1/clients} issue distinct API keys.
     * 
     * @author James Chen, Codex
     */
    @Test
    void separateClientsReceiveDistinctApiKeys() throws Exception {
        final String firstApiKey = getApiKey();
        final String secondApiKey = getApiKey();
        assertNotEquals(firstApiKey, secondApiKey);
    }
}
