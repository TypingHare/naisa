package com.naisa.util;

import org.springframework.test.web.servlet.MvcResult;

import com.naisa.common.Capsule;

/**
 * Pairs a raw MockMvc result with the capsule parsed from its response body.
 *
 * @param <T>       type of the capsule's {@code data} field
 * @param mvcResult the raw result, including the HTTP status, headers, and body
 * @param capsule   the response body deserialized as a {@link Capsule}
 * @author James Chen
 */
public record MvcResponse<T>(MvcResult mvcResult, Capsule<T> capsule) {
}
