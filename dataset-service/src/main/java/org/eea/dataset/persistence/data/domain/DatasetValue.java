package org.eea.dataset.persistence.data.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.hibernate.annotations.DynamicUpdate;

/**
 * The type Dataset.
 */
@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(name = "DATASET_VALUE")
public class DatasetValue {


  /**
   * Init.
   */
  @PostLoad
  public void init() {
    //determine level error in validations
    if (null != this.datasetValidations && this.datasetValidations.size() > 0) {
      for (DatasetValidation datasetValidation : this.datasetValidations) {
        if (datasetValidation.getValidation().getLevelError().equals(TypeErrorEnum.ERROR)) {
          this.levelError = TypeErrorEnum.ERROR;
          break;
        } else {
          this.levelError = datasetValidation.getValidation().getLevelError();
        }
      }
    }
  }

  /**
   * The id.
   */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /**
   * The id mongo.
   */
  @Column(name = "ID_DATASET_SCHEMA")
  private String idDatasetSchema;

  /**
   * The table values.
   */
  @OneToMany(mappedBy = "datasetId", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<TableValue> tableValues;

  /**
   * The field validations.
   */
  @OneToMany(mappedBy = "datasetValue", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<DatasetValidation> datasetValidations;
  
  @Transient
  private TypeErrorEnum levelError;

  /**
   * return Objects.hash(id, tableValues, idRecordSchema); Equals.
   *
   * @param object the object
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    final DatasetValue dataset = (DatasetValue) object;
    return id.equals(dataset.id) && idDatasetSchema.equals(dataset.idDatasetSchema)
        && tableValues.equals(dataset.tableValues);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, tableValues);
  }

}
