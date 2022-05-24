package org.eea.validation.util.model;

import org.kie.api.KieBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Validation process vo.
 */
@Getter
@Setter
@AllArgsConstructor
public class ValidationProcessVO {

  /** The kie base. */
  private KieBase kieBase;

  /** The requesting user. */
  private String requestingUser;
}
