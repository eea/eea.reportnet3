package org.eea.validation.util.model;

import java.util.Deque;
import org.eea.kafka.domain.EEAEventVO;
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

  /** The pending oks. */
  private Integer pendingOks;

  /** The pending validations. */
  private Deque<EEAEventVO> pendingValidations;

  /** The kie base. */
  private KieBase kieBase;

  /** The coordinator process. */
  private boolean coordinatorProcess;

  /** The requesting user. */
  private String requestingUser;

  /** The released. */
  private boolean released;
}
