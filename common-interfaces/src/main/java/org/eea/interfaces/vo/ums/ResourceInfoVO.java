package org.eea.interfaces.vo.ums;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;

/**
 * The type Group info vo.
 */
@Getter
@Setter
@ToString
public class ResourceInfoVO {

  /**
   * The resource id.
   */
  private Long resourceId;

  /**
   * The name.
   */
  private String name;

  /**
   * The path.
   */
  private String path;

  /**
   * The Resource type enum.
   */
  private ResourceTypeEnum resourceTypeEnum;
  /**
   * The Attributes.
   */
  private Map<String, List<String>> attributes;


  /**
   * The Security role enum.
   */
  private SecurityRoleEnum securityRoleEnum;
}
