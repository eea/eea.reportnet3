package org.eea.validation.util.model;

import java.util.Deque;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eea.kafka.domain.EEAEventVO;
import org.kie.api.KieBase;

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

}
