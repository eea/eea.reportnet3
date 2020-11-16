package org.eea.dataflow.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import lombok.Data;

/**
 * The type Dataflow.
 */
@Entity
@Data
public class DataflowStatusDataset {

  /** The id. */
  @Id
  @Column(name = "ID")
  private Long id;

  /** The status. */
  @Column(name = "STATUS")
  private DatasetStatusEnum status;

}
