package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;


/**
 * The Interface DataflowWithRequestType.
 */
public interface DataflowWithRequestType {

  /**
   * Gets the dataflow.
   *
   * @return the dataflow
   */
  Dataflow getDataflow();

  /**
   * Gets the type request enum.
   *
   * @return the type request enum
   */
  TypeRequestEnum getTypeRequestEnum();
}
