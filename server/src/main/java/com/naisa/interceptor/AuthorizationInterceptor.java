package com.naisa.interceptor;

import com.naisa.common.Capsule;
import com.naisa.common.annotation.AuthorizationRequired;
import com.naisa.exception.ClientException;
import com.naisa.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

/**
 * This interceptor verifies user authorization when required.
 *
 * <p>Before the controller method (also known as the handler method) handles the request, this
 * interceptor checks whether the method is annotated with {@code AuthorizationRequired}. If so, it
 * extracts the API key from the "Authorization" header and checks whether a client is associated
 * with that key.
 *
 * <p>A valid Authorization header value must start with "Bearer ". If the header is invalid, the
 * interceptor builds a response and prevents the controller method from handling the request. The
 * response contains a capsule with the error code {@code ERROR_CODE_INVALID_AUTHORIZATION_HEADER}.
 *
 * <p>If the API key is missing or is not associated with any client, the interceptor builds a
 * response and prevents the controller method from handling the request. The response contains a
 * capsule with the error code {@code ERROR_CODE_NO_API_KEY_PROVIDED}.
 *
 * @author James Chen
 */
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
  private ClientService clientService;

  public static final String ERROR_CODE_INVALID_AUTHORIZATION_HEADER =
      "authorization.invalid_authorization_header";

  public static final String ERROR_CODE_NO_API_KEY_PROVIDED = "authorization.no_api_key_provided";

  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  private AuthorizationInterceptor(final ClientService clientService) {
    this.clientService = clientService;
  }

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler)
      throws Exception {
    if (handler instanceof HandlerMethod method) {
      if (method.hasMethodAnnotation(AuthorizationRequired.class)) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null) {
          final Capsule<Object> capsule =
              new Capsule<>("No API key provided.", null, ERROR_CODE_NO_API_KEY_PROVIDED);
          buildResponse(response, capsule);
          return false;
        }

        final int authorizationHeaderPrefixLength = AUTHORIZATION_HEADER_PREFIX.length();
        if (authorizationHeader.length() <= authorizationHeaderPrefixLength
            || !authorizationHeader
                .substring(0, authorizationHeaderPrefixLength)
                .equals(AUTHORIZATION_HEADER_PREFIX)) {
          final Capsule<Object> capsule =
              new Capsule<>(
                  "Invalid Authorization header.", null, ERROR_CODE_INVALID_AUTHORIZATION_HEADER);
          buildResponse(response, capsule);
          return false;
        }

        final String apiKey =
            authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length()).trim();
        try {
          clientService.getClientByApiKey(apiKey);
        } catch (ClientException e) {
          buildResponse(response, Capsule.fromServiceException(e));
          return false;
        }
      }
    }

    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  /**
   * Writes the capsule as a UTF-8 JSON response with HTTP status 401 (Unauthorized).
   *
   * @param response the servlet response to populate
   * @param capsule the response payload to serialize as JSON
   * @throws IOException if writing the response body fails
   */
  private void buildResponse(final HttpServletResponse response, final Capsule<Object> capsule)
      throws IOException {
    final String body = new ObjectMapper().writeValueAsString(capsule);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.getWriter().write(body);
  }
}
