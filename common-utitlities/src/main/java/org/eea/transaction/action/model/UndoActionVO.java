package org.eea.transaction.action.model;


import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Undo action vo. The subclasses of this class will contain the required information to
 * undo an action
 */
@Getter
@Setter
public class UndoActionVO implements Serializable {

  private static final long serialVersionUID = -7434680474632896343L;

  private Exception exception;
}
