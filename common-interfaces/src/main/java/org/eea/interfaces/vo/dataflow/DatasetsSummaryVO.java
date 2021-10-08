package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DatasetsSummaryVO
 */
@Getter
@Setter
@ToString
public class DatasetsSummaryVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  private Long id;

  /** The data set name. */
  private String dataSetName;

  /** The dataset type enum. */
  private DatasetTypeEnum datasetTypeEnum;

  /** The data provider code. */
  private String dataProviderCode;

  /** The data provider name. */
  private String dataProviderName;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataProviderCode == null) ? 0 : dataProviderCode.hashCode());
    result = prime * result + ((dataProviderName == null) ? 0 : dataProviderName.hashCode());
    result = prime * result + ((dataSetName == null) ? 0 : dataSetName.hashCode());
    result = prime * result + ((datasetTypeEnum == null) ? 0 : datasetTypeEnum.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
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
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DatasetsSummaryVO other = (DatasetsSummaryVO) obj;
    if (dataProviderCode == null) {
      if (other.dataProviderCode != null)
        return false;
    } else if (!dataProviderCode.equals(other.dataProviderCode))
      return false;
    if (dataProviderName == null) {
      if (other.dataProviderName != null)
        return false;
    } else if (!dataProviderName.equals(other.dataProviderName))
      return false;
    if (dataSetName == null) {
      if (other.dataSetName != null)
        return false;
    } else if (!dataSetName.equals(other.dataSetName))
      return false;
    if (datasetTypeEnum != other.datasetTypeEnum)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
