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

  private Integer pendingOks;
  private Deque<EEAEventVO> pendingValidations;
  private KieBase kieBase;
  private boolean coordinatorProcess;
  private String requestingUser;
  private boolean released;
}
