package org.eea.interfaces.vo.ums;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TokenVO {

  private String accessToken;
  private String refreshToken;
}
