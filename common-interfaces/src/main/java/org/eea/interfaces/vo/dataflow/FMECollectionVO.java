package org.eea.interfaces.vo.dataflow;

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
public class FMECollectionVO implements Serializable {

  private static final long serialVersionUID = -4563147962237980165L;

  private Integer limit;
  private Integer offset;
  private Integer totalCount;
  private List<FMEItemVO> items;

}
