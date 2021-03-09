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
public class DataflowUserRoleVO {

  /** The dataflow id. */
  private Long dataflowId;

  /** The dataflow name. */
  private String dataflowName;

  /** The users. */
  private List<UserRoleVO> users;

}
