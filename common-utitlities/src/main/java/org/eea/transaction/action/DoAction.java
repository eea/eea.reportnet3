package org.eea.transaction.action;

import org.eea.exception.EEAException;
import org.eea.transaction.action.model.UndoActionVO;

/**
 * The interface Do action.
 */
@FunctionalInterface
public interface DoAction {

  /**
   * Do action based on incoming args
   *
   * @param args the args
   *
   * @return the undo action vo
   */
  UndoActionVO doAction(Object... args);
}
