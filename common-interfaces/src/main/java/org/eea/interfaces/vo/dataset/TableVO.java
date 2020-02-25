package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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


  /** The level error. */
  private ErrorTypeEnum levelError;

  /**
   * The total records.
   */
  private Long totalRecords;


  /**
   * The total records.
   */
  private Long totalFilteredRecords;

  /**
   * The validations.
   */
  private List<TableValidationVO> tableValidations = new ArrayList<TableValidationVO>();


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
