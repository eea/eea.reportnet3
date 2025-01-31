package org.eea.interfaces.vo.ums;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * The Class UserNationalCoordinatorVO.
 */
@Getter
@Setter
@ToString
public class UserNationalCoordinatorVO implements Serializable {

  private static final long serialVersionUID = -7108215109717761920L;
  /** The email. */
  private String email;

  /** The country code. */
  private String countryCode;

}
