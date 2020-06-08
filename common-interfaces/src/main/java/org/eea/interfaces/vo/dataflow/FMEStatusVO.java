package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FMEStatusVO implements Serializable {

  private static final long serialVersionUID = -1033879262267477894L;

  private String status;

}
