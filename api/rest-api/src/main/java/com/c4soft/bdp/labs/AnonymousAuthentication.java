package com.c4soft.bdp.labs;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class AnonymousAuthentication extends AbstractAuthenticationToken {
  private static final long serialVersionUID = -9124969832489565296L;

  public static final AnonymousAuthentication instance = new AnonymousAuthentication();

  private AnonymousAuthentication() {
    super(List.of());
  }

  @Override
  public @Nullable Object getCredentials() {
    return null;
  }

  @Override
  public @Nullable Object getPrincipal() {
    return null;
  }

}