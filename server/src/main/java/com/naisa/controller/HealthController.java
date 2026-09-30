package com.naisa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health controller.
 *
 * <p>This controller provides a single method, {@code check()}, that allows clients to easily check
 * whether the server is currently running.
 *
 * @author James Chen
 */
@RestController
@RequestMapping("/health")
public class HealthController {
  @GetMapping
  public ResponseEntity<String> check() {
    return ResponseEntity.ok("Okay");
  }
}
