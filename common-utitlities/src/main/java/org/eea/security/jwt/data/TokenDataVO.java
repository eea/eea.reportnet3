package org.eea.security.jwt.data;

import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Token data vo.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TokenDataVO {

  /**
   * The Other claims.
   */
  private Map<String, Object> otherClaims;
  /**
   * The Roles.
   */
  private Set<String> roles;
  /**
   * The User id.
   */
  private String userId;
  /**
   * The Preferred username.
   */
  private String preferredUsername;

  /**
   * The Expiration.
   */
  private Integer expiration;


}
