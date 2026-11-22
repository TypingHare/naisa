package com.naisa.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.naisa.interceptor.LoggingInterceptor;

import tools.jackson.databind.json.JsonMapper;

/**
 * Provides the shared Spring test configuration and MockMvc setup for API integration tests.
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

    /**
     * Disables request and response logging in {@link LoggingInterceptor} before each test.
     *
     * @author James Chen
     */
    @BeforeEach
    void disableRequestAndResultLogging() {
        loggingInterceptor.setLogRequestAndResult(false);
    }
}
