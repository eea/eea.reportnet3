package org.eea.dataset.service.model;

import org.eea.dataset.persistence.data.domain.FieldValue;


/**
 * The Interface FieldValueWithLabelProjection. Used by the method
 * findByIdFieldSchemaAndConditionalWithTag in the FieldRepository as a projection
 */
public interface FieldValueWithLabelProjection {

  /**
   * Gets the field value.
   *
   * @return the field value
   */
  FieldValue getFieldValue();


  /**
   * Gets the label.
   *
   * @return the label
   */
  FieldValue getLabel();

}
