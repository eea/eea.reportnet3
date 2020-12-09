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

  /** The write permission. */
  private Boolean writePermission;

  /** The data provider id. */
  private Long dataProviderId;


  /**
   * @return the account
   */
  public String getAccount() {
    return null != account ? account.toLowerCase() : null;
  }

  /**
   * @param account the account to set
   */
  public void setAccount(String account) {
    this.account = null != account ? account.toLowerCase() : null;
  }

}
