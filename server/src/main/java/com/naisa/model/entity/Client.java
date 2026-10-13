package com.naisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * Represents a client entity.
 *
 * <p>A client refers to a company or organization that uses this service, instead of an end user.
 *
 * <p>Some operations in this service requires API key. This service will check if the provided API
 * key exists in this table. If the API key does not exist, it will be considered invalid.
 *
 * @author James Chen
 */
@JavaBean
@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Generated(event = EventType.INSERT)
  @ColumnDefault("gen_random_uuid()::text")
  @Column(name = "api_key", length = 36, nullable = false, unique = true)
  @Size(max = 36, message = "API key must not exceed 36 characters")
  private String apiKey;
}
