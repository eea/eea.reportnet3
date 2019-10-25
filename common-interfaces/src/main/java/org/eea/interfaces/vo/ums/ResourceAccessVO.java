package org.eea.interfaces.vo.ums;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;

/**
 * The type Resource access vo.
 */
@Getter
@Setter
@ToString
public class ResourceAccessVO implements Serializable {

  private static final long serialVersionUID = -325425386997089761L;
  private ResourceTypeEnum resource;
  private SecurityRoleEnum role;
  private Long id;
}
