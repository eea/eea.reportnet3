package org.eea.multitenancy;

import org.eea.exception.EEAException;

/**
 * The interface Proxy test interface.
 */
public interface ProxyTestInterface {

  /**
   * Test method.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException the eea exception
   */
  void testMethod(@DatasetId String datasetId) throws EEAException;

}
