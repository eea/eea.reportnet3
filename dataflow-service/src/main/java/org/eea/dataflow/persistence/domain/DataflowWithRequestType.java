package org.eea.dataflow.persistence.domain;

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


  /**
   * Gets the request id.
   *
   * @return the request id
   */
  Long getRequestId();
}
