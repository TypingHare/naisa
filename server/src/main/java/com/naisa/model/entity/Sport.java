package com.naisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a sport entity.
 *
 * @author James Chen, Vincent Lai
 */
@JavaBean
@Entity(name = "sports")
@Getter
@Setter
public class Sport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(
      name = "name",
      length = 32,
      nullable = false,
      comment = "Name of the sport in title case. For example: Basketball, Table Tennis.")
  @Size(max = 32, message = "Sport name must not exceed 32 characters")
  private String name;
}
