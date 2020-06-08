package org.eea.dataflow.integration.fme.domain;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class FMEStatus implements Serializable {

  private static final long serialVersionUID = 7465269976253629719L;

  private String status;
}
