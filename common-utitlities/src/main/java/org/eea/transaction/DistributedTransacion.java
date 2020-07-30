package org.eea.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.eea.exception.EEAException;
import org.eea.transaction.action.DoAction;
import org.eea.transaction.action.UndoAction;
import org.eea.transaction.action.model.UndoActionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Distributed transacion.
 */
@Getter
@Setter
@Slf4j
public class DistributedTransacion {

  /** The transaction id. */
  private UUID transactionId;

  /** The do actions. */
  private List<DoAction> doActions;

  /** The undo actions. */
  private List<UndoAction> undoActions;

  /** The action args. */
  private List<Object[]> actionArgs;

  /** The undo action VOS. */
  private List<UndoActionVO> undoActionVOS;

  /** The Constant ERROR_LOG. */
  private static final Logger ERROR_LOG = LoggerFactory.getLogger("error_logger");

  /**
   * Instantiates a new Distributed transacion.
   *
   * @param transactionId the transaction id
   */
  public DistributedTransacion(UUID transactionId) {
    this.transactionId = transactionId;
    doActions = new ArrayList<>();
    undoActions = new ArrayList<>();
    actionArgs = new ArrayList<>();
    undoActionVOS = new ArrayList<>();

  }

  /**
   * Add action to the saga to be executed.
   *
   * @param action the action
   * @param undoAction the undo action
   * @param args the args to execute the action
   */
  public void addAction(DoAction action, UndoAction undoAction, Object... args) {
    doActions.add(action);
    undoActions.add(undoAction);
    actionArgs.add(args);
  }


  /**
   * Execute distributed transaction with previously defined actions.
   *
   * @throws EEAException the eea exception
   */
  public void executeTransaction() throws EEAException {
    Assert.isTrue(doActions.size() == undoActions.size(),
        "Error, actions list size is different than undo acction list size");
    Assert.isTrue(doActions.size() == actionArgs.size(),
        "Error, actions list size is different than acction args list size");

    // Set up the saga pipeline execution
    int stepsNumber = doActions.size();
    int curentStep = 0;
    List<UndoActionVO> undoActionVOS = new ArrayList<>();
    boolean rollback = false;
    // Running saga pipeline execution
    for (; curentStep < stepsNumber; curentStep++) {
      try {
        undoActionVOS.add(doActions.get(curentStep).doAction(actionArgs.get(curentStep)));
      } catch (Exception e) {
        ERROR_LOG.error("Error executing distributed transaction {} due to {}", this.transactionId,
            e.getMessage(), e);
        rollback = true;
        break;
      }
    }

    // checking whether to perform rollback or not
    if (rollback) {
      log.warn("Rolling back transaction {}", this.transactionId);
      // last step was not performed/committed, so starting rollback from previous one
      curentStep--;
      for (; curentStep > 0; curentStep--) {
        undoActions.get(curentStep).undoAction(undoActionVOS.get(curentStep));
      }
    } else {
      log.info("Transaction {} finished successfully", this.transactionId);
    }

  }
}
