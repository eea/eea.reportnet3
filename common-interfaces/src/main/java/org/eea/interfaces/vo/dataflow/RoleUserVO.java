package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RoleUserVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 98399267263132919L;

  /** The account email. */
  private String account;

  /** The role. */
  private String role;

  /** The write permission. */
  private Boolean permission;

  /** The data provider id. */
  private Long dataProviderId;
}
