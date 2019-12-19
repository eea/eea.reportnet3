package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;

/**
 * The Interface DataProviderCodeVO.
 */
public interface DataProviderCodeVO extends Serializable {

  /**
   * Gets the data provider group id.
   *
   * @return the data provider group id
   */
  String getDataProviderGroupId();

  /**
   * Gets the label.
   *
   * @return the label
   */
  String getLabel();
}
