package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class DataSetVO implements Serializable {

  private static final long serialVersionUID = 2680945261242083928L;
  private String id;

  private List<RecordVO> records;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public List<RecordVO> getRecords() {
    return records;
  }

  public void setRecords(final List<RecordVO> records) {
    this.records = records;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataSetVO dataSetVO = (DataSetVO) o;
    return id.equals(dataSetVO.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, records);
  }

  @Override
  public String toString() {
    return "DataSetVO{" +
        "id='" + id + '\'' +
        ", records=" + records +
        '}';
  }
}
