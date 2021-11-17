package org.eea.interfaces.vo.contributor;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ContributorVO.
 */
@Getter
@Setter
@ToString
public class ContributorVO implements Serializable {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = -6722104829767052100L;

  /** The account email. */
  private String account;

  /** The role. */
  private String role;

  /** The data provider id. */
  private Long dataProviderId;

  /** Is the contributor temporary?. */
  private boolean invalid;

}
