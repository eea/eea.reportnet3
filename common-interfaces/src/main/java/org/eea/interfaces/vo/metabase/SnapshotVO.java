package org.eea.interfaces.vo.metabase;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class SnapshotVO.
 */
@Getter
@Setter
@ToString
public class SnapshotVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1860518284289153706L;

  /** The id. */
  private Long id;

  /** The description. */
  private String description;

  /** The creation date. */
  private Date creationDate;

  /** The release. */
  private Boolean release;

  /** The blocked. */
  private Boolean blocked;

}
