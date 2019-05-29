package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO: Auto-generated Javadoc
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
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TableVO other = (TableVO) obj;
    if (headers == null) {
      if (other.headers != null)
        return false;
    } else if (!headers.equals(other.headers))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (idMongo == null) {
      if (other.idMongo != null)
        return false;
    } else if (!idMongo.equals(other.idMongo))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (records == null) {
      if (other.records != null)
        return false;
    } else if (!records.equals(other.records))
      return false;
    return true;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((headers == null) ? 0 : headers.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((idMongo == null) ? 0 : idMongo.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((records == null) ? 0 : records.hashCode());
    return result;
  }

}
