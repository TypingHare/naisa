package com.naisa.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * This interceptor logs each request before and after it is handled.
 *
 * <p>Before the request is handled, it creates an INFO log entry containing the HTTP method,
 * request path, and handler (the simple class name and controller method name).
 *
 * <p>After the request is handled, it creates an INFO log entry containing the HTTP method, request
 * path, and response time in milliseconds.
 *
 * <p>Below is an example of a log message before the request is handled:
 *
 * {@snippet :
 *     POST /v1/clients handled by ClientController:issueApiKey
 * }
 *
 * <p>Below is an example of a log message after the request is handled:
 *
 * {@snippet :
 *     POST /v1/clients handled in 55 milliseconds
 * }
 *
 * <p>It keeps track of a request by storing an {@code Instant} object as a request attribute in the
 * {@code preHandle} method and retrieving it in the {@code postHandle} method to calculate the
 * response time.
 *
 * @author James Chen
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

  private static final String KEY_REQUEST_RECEIVED_TIMESTAMP = "requestReceivedTimestamp";

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler)
      throws Exception {
    if (handler instanceof HandlerMethod method) {
      LOGGER.info(
          "{} {} handled by {}:{}",
          request.getMethod().toUpperCase(),
          request.getRequestURI(),
          method.getBeanType().getSimpleName(),
          method.getMethod().getName());

      request.setAttribute(KEY_REQUEST_RECEIVED_TIMESTAMP, Instant.now());
    }

    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  @Override
  public void postHandle(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      @Nullable final ModelAndView modelAndView)
      throws Exception {

    if (handler instanceof HandlerMethod) {
      final Instant requestReceivedInstant =
          (Instant) request.getAttribute(KEY_REQUEST_RECEIVED_TIMESTAMP);
      final Duration responseTime = Duration.between(requestReceivedInstant, Instant.now());

      LOGGER.info(
          "{} {} handled in {} milliseconds",
          request.getMethod().toUpperCase(),
          request.getRequestURI(),
          String.valueOf(responseTime.toMillis()));
    }

    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }
}
