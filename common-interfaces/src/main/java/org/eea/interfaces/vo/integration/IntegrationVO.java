package org.eea.interfaces.vo.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class IntegrationVO.
 */
@Getter
@Setter
@ToString
public class IntegrationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1348263779137653665L;

  /** The id. */
  private Long id;

  /** The name. */
  private String name;

  /** The description. */
  private String description;

  /** The tool. */
  private IntegrationToolTypeEnum tool;

  /** The operation. */
  private IntegrationOperationTypeEnum operation;

  /** The internal parameters. */
  private Map<String, String> internalParameters = new HashMap<>();

  /** The external parameters. */
  private Map<String, String> externalParameters = new HashMap<>();
}
