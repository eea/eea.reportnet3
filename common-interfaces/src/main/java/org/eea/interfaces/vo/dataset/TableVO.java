package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableVO.
 */
@Getter
@Setter
@ToString
public class TableVO implements Serializable {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 2680945261242083928L;

  /**
   * The id.
   */
  private Long id;

  /**
   * The id mongo.
   */
  private String idTableSchema;

  /**
   * The records.
   */
  private List<RecordVO> records;


  /**
   * The total records.
   */
  private Long totalRecords;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, idTableSchema, records);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final TableVO table = (TableVO) obj;
    return id.equals(table.id) && idTableSchema.equals(table.idTableSchema)
        && records.equals(table.records);
  }
}
