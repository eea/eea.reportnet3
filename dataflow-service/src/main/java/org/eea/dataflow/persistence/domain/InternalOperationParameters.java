package org.eea.dataflow.persistence.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * The Class InternalOperationParameters.
 */
@Entity
@DiscriminatorValue(value = "INTERNAL")
public class InternalOperationParameters extends OperationParameters {


}
