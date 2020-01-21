package org.eea.transaction.action;


import org.eea.transaction.action.model.UndoActionVO;

/**
 * The interface Undo action.
 */
@FunctionalInterface
public interface UndoAction {

  /**
   * Undo an action based on data coming in UndoActionVO.
   *
   * @param undoActionVO the undo action vo
   */
  void undoAction(UndoActionVO undoActionVO);
}
