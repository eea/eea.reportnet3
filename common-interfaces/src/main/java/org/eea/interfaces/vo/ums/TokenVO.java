package org.eea.interfaces.vo.ums;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class TokenVO.
 */
@Getter
@Setter
@ToString
public class TokenVO {

  /** The access token. */
  private String accessToken;

  /** The refresh token. */
  private String refreshToken;
}
