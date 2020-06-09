package org.eea.dataflow.integration.executor.fme.domain;

import java.io.Serializable;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class FMECollection implements Serializable {

  private static final long serialVersionUID = 929963618065871570L;

  private Integer limit;
  private Integer offset;
  private Integer totalCount;
  private List<FMEItem> items;

}
