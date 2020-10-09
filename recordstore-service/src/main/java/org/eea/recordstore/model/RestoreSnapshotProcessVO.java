package org.eea.recordstore.model;

import java.util.Deque;
import org.eea.kafka.domain.EEAEventVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Validation process vo.
 */
@Getter
@Setter
@AllArgsConstructor
public class RestoreSnapshotProcessVO {

  private Deque<EEAEventVO> pendingRestorations;
  private String requestingUser;

}
