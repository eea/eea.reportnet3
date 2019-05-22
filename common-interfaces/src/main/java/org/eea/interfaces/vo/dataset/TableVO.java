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

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2680945261242083928L;

  /** The id. */
  private Long id;

  /** The id mongo. */
  private String idMongo;

  /** The records. */
  private List<RecordVO> records;

  /** The headers. */
  private List<FieldSchemaVO> headers;

  /** The name. */
  private String name;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, idMongo, records, headers);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TableVO table = (TableVO) obj;
    return id.equals(table.id) && name.equals(table.name) && idMongo.equals(table.idMongo)
        && records.equals(table.records) && headers.equals(table.headers);
  }
}
