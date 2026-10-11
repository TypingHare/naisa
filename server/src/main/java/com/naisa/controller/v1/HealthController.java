package com.naisa.controller.v1;

import com.naisa.common.Capsule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Represents the health controller.
 *
 * <p>This controller provides a single method, {@code check()}, that allows clients to easily check
 * whether the server is currently running.
 *
 * @author James Chen
 */
@RestController
@RequestMapping("/v1/health")
public class HealthController {
  /**
   * Checks whether the service is running.
   *
   * @return a response entity with no content
   */
  @GetMapping
  public ResponseEntity<Capsule<Object>> check() {
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Capsule<>("", null));
  }
}
