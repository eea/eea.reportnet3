package org.eea.dataset.service.model;

import org.eea.dataset.persistence.data.domain.FieldValue;

public interface FieldValueWithLabel {

  /**
   * Gets the dataflow.
   *
   * @return the dataflow
   */
  FieldValue getFieldValue();

  /**
   * Gets the type request enum.
   *
   * @return the type request enum
   */
  FieldValue getLabel();



}
