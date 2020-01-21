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

}
