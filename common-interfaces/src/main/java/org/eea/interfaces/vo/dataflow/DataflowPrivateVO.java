package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataflowPrivateVO extends GenericDataflowVO implements Serializable {
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8073212422480973637L;

  /** The documents. */
  private List<DocumentVO> documents;

  /** The weblinks. */
  private List<WeblinkVO> weblinks;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataflowPrivateVO that = (DataflowPrivateVO) o;
    return id.equals(that.id);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, name, deadlineDate, weblinks, documents, obligation,
        status, releasable);
  }
}
