package org.eea.dataflow.integration.executor.fme.domain;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DirectoryName.
 */
@Getter
@Setter
@ToString
public class DirectoryName implements Serializable {

  private static final long serialVersionUID = -3698654345781117269L;

  /** The directoryname. */
  private String directoryname;
}
