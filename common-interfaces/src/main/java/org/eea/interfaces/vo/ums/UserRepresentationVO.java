package org.eea.interfaces.vo.ums;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UserRepresentationVO.
 */
@Getter
@Setter
@ToString
public class UserRepresentationVO {

  /** The username. */
  private String username;

  /** The email. */
  private String email;

  /** The first name. */
  private String firstName;

  /** The last name. */
  private String lastName;

  private Map<String, List<String>> attributes;

}
