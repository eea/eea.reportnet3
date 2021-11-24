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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + ((dataProviderId == null) ? 0 : dataProviderId.hashCode());
    result = prime * result + (invalid ? 1231 : 1237);
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ContributorVO other = (ContributorVO) obj;
    if (account == null) {
      if (other.account != null)
        return false;
    } else if (!account.equals(other.account))
      return false;
    if (dataProviderId == null) {
      if (other.dataProviderId != null)
        return false;
    } else if (!dataProviderId.equals(other.dataProviderId))
      return false;
    if (invalid != other.invalid)
      return false;
    if (role == null) {
      if (other.role != null)
        return false;
    } else if (!role.equals(other.role))
      return false;
    return true;
  }
}
