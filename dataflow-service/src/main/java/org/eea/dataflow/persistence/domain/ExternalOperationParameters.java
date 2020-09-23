package org.eea.dataflow.persistence.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * The Class ExternalOperationParameters.
 */
@Entity
@DiscriminatorValue(value = "EXTERNAL")
public class ExternalOperationParameters extends OperationParameters {

}
