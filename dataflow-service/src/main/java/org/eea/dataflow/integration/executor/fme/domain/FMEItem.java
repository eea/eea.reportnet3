package org.eea.dataflow.integration.executor.fme.domain;

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
public class FMEItem implements Serializable {

  private static final long serialVersionUID = -5074902098646538656L;

  private String description;
  private String lastPublishDate;
  private String lastSaveDate;
  private String name;
  private String repositoryName;
  private String title;
  private FMEItemEnum type;
  private String userName;

}
