package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The type Data flow vo.
 */
public class DataFlowVO implements Serializable {

  private static final long serialVersionUID = -8073212422480973637L;
  private String id;
  private List<DataSetVO> datasets;

  /**
   * Gets datasets.
   *
   * @return the datasets
   */
  public List<DataSetVO> getDatasets() {
    return datasets;
  }

  /**
   * Sets datasets.
   *
   * @param datasets the datasets
   */
  public void setDatasets(final List<DataSetVO> datasets) {
    this.datasets = datasets;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }


  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(final String id) {
    this.id = id;
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataFlowVO that = (DataFlowVO) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, datasets);
  }

  @Override
  public String toString() {
    return "DataFlowVO{" +
        "id='" + id + '\'' +
        ", datasets=" + datasets +
        '}';
  }
}
