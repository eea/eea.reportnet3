package org.eea.communication.configuration;

import java.security.Principal;

/**
 * The Class StompPrincipal.
 */
public class StompPrincipal implements Principal {

  /** The name. */
  private String name;

  /**
   * Instantiates a new stomp principal.
   *
   * @param name the name
   */
  public StompPrincipal(String name) {
    this.name = name;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return name;
  }
}
