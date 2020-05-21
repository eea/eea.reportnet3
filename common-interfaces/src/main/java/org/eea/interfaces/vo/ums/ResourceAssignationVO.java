package org.eea.interfaces.vo.ums;

import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type ResourceAssignationVO.
 */
@Getter
@Setter
@ToString
public class ResourceAssignationVO {


  /**
   * The resource id.
   */
  private Long resourceId;


  /** The email. */
  private String email;


  /** The resource group. */
  private ResourceGroupEnum resourceGroup;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((resourceGroup == null) ? 0 : resourceGroup.hashCode());
    result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
    return result;
  }


  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ResourceAssignationVO other = (ResourceAssignationVO) obj;
    if (email == null) {
      if (other.email != null) {
        return false;
      }
    } else if (!email.equals(other.email)) {
      return false;
    }
    if (resourceGroup != other.resourceGroup) {
      return false;
    }
    if (resourceId == null) {
      if (other.resourceId != null) {
        return false;
      }
    } else if (!resourceId.equals(other.resourceId)) {
      return false;
    }
    return true;
  }

}
