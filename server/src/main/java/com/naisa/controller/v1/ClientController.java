package com.naisa.controller.v1;

import com.naisa.common.Capsule;
import com.naisa.model.entity.Client;
import com.naisa.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Represents the client controller.
 *
 * <p>This controller provides a single method, {@code issueApiKey()}, that creates a client and
 * issues an API key.
 */
@RestController
@RequestMapping("/v1/clients")
public class ClientController {
  private final ClientService clientService;

  public ClientController(final ClientService clientService) {
    this.clientService = clientService;
  }

  /**
   * Creates a client by issuing an API key.
   *
   * @return a response entity containing the created client.
   * @author James Chen
   */
  @PostMapping
  public ResponseEntity<Capsule<Client>> issueApiKey() {
    final Client createdClient = clientService.createClient();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new Capsule<>("API key has been issued.", createdClient));
  }
}
