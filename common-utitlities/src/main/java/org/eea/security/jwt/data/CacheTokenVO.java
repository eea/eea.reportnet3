package org.eea.security.jwt.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class CacheTokenVO.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CacheTokenVO {

  /**
   * The access token.
   */
  private String accessToken;

  /**
   * The refresh token.
   */
  private String refreshToken;
}
