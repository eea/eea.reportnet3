package org.eea.dataset.service.model;

import org.eea.dataset.persistence.data.domain.FieldValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class FieldValueWithLabel.
 */
@Getter
@Setter
@ToString
public class FieldValueWithLabel {

  /** The field value. */
  private FieldValue fieldValue;

  /** The label. */
  private FieldValue label;

  /** The order. */
  private Integer order;

}
