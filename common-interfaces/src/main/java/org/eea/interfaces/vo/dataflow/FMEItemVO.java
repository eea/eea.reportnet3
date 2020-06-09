package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.dataflow.enums.FMEItemEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class FMEItemVO implements Serializable {

  private static final long serialVersionUID = 4000763864098810155L;

  private String description;
  private String lastPublishDate;
  private String lastSaveDate;
  private String name;
  private String repositoryName;
  private String title;
  private FMEItemEnum type;
  private String userName;

}
