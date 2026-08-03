package com.c4soft.resthero.customer.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Customer {

  @EqualsAndHashCode.Include
  @ToString.Include
  private String id;

  @ToString.Include
  private final String firstName;

  @ToString.Include
  private final String lastName;

  private final String email;

}
