package com.naisa.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.naisa.common.Capsule;
import com.naisa.interceptor.LoggingInterceptor;
import com.naisa.model.entity.Client;
import com.naisa.repository.ClientRepository;
import com.naisa.util.MvcResponse;
import com.naisa.util.Route;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base class for API integration tests that use {@link MockMvc} with the Spring test profile.
 *
 * @author James Chen
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
abstract class IntegrationTest {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private ClientRepository clientRepository;

    private String testApiKey;

    /**
     * Disables request and response logging in {@link LoggingInterceptor} before each test.
     *
     * @author James Chen
     */
    @BeforeEach
    void disableRequestAndResultLogging() {
        loggingInterceptor.setLogRequestAndResult(false);
    }

    /**
     * Loads the API key for the seeded client with ID 1 before each test.
     *
     * @author James Chen
     */
    @BeforeEach
    void setTestApiKey() {
        final Client client = clientRepository.findById(1L).orElseThrow(
            () -> new RuntimeException("Test database does not have a client of ID 1.")
        );

        testApiKey = client.getApiKey();
    }

    /**
     * Sends a request using the seeded client's bearer token. Use {@link MvcResponse#mvcResult()}
     * to inspect the HTTP status and headers, and {@link MvcResponse#capsule()} to read the parsed
     * response body. If the body omits {@code data}, the capsule's data is {@code null}.
     *
     * @param <T>      type of the response's {@code data} field
     * @param route    HTTP method and path to request
     * @param dataType class used to deserialize the {@code data} field
     * @return the raw MockMvc result and the typed capsule for the same request
     * @throws Exception if the request fails to execute or the response cannot be deserialized
     * @author James Chen
     */
    protected <T> MvcResponse<T> request(final Route route, final Class<T> dataType)
        throws Exception {
        final MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.request(route.method(), route.path()).header(
                "Authorization",
                "Bearer " + testApiKey
            )
        ).andReturn();
        final String content = mvcResult.getResponse().getContentAsString();
        final JsonNode jsonNode = jsonMapper.readTree(content);
        final String message = jsonNode.path("message").asString(null);
        final String errorCode = jsonNode.path("errorCode").asString(null);
        final JsonNode dataNode = jsonNode.get("data");
        final T data = dataNode == null ? null : jsonMapper.treeToValue(dataNode, dataType);

        return new MvcResponse<>(mvcResult, new Capsule<>(message, data, errorCode));
    }
}
