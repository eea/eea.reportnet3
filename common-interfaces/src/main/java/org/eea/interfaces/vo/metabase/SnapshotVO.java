package org.eea.interfaces.vo.metabase;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SnapshotVO implements Serializable {

  private static final long serialVersionUID = -1860518284289153706L;

  private Long id;

  private String description;

  private Date creationDate;

}
