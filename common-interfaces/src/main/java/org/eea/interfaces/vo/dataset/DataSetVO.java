package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DataSetVO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataSetVO implements Serializable {

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
  private String idDatasetSchema;

  /**
   * The table VO.
   */
  private List<TableVO> tableVO;

  /** The level error. */
  private ErrorTypeEnum levelError;

  /**
   * The validations.
   */
  private List<DatasetValidationVO> datasetValidations = new ArrayList<DatasetValidationVO>();

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, tableVO, idDatasetSchema);
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
    final DataSetVO other = (DataSetVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(tableVO, other.tableVO)
        && Objects.equals(idDatasetSchema, other.idDatasetSchema);
  }


}
