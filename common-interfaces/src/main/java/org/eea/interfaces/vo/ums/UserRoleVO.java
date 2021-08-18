package org.eea.interfaces.vo.ums;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UserRoleVO.
 */
@Getter
@Setter
@ToString
public class UserRoleVO {

  /** The email. */
  private String email;

  /** The roles. */
  private List<String> roles;

  /** The country. */
  private String country;

}
