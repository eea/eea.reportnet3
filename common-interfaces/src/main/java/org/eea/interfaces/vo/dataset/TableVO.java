package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;

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
   * The headers.
   */
  private List<FieldSchemaVO> headers;

  /**
   * The name.
   */
  private String name;

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
    return Objects.hash(id, name, idTableSchema, records, headers);
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
    return id.equals(table.id) && name.equals(table.name) && idTableSchema
        .equals(table.idTableSchema)
        && records.equals(table.records) && headers.equals(table.headers);
  }
}
