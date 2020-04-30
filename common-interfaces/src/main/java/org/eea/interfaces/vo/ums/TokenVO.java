package org.eea.interfaces.vo.ums;

import java.io.Serializable;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class TokenVO.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TokenVO implements Serializable {

  private static final long serialVersionUID = -5557056744359972573L;
  /**
   * The access token.
   */
  private String accessToken;

  /**
   * The refresh token.
   */
  private String refreshToken;

  /**
   * the roles
   */
  private Set<String> roles;
  /**
   * the groups
   */
  private Set<String> groups;

  /**
   * the access token expiration
   */
  private Integer accessTokenExpiration;

  /**
   * the preferred username
   */
  private String preferredUsername;
  /**
   * the user id
   */
  private String userId;

}
