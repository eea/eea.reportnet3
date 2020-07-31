package org.eea.dataset.mapper;

import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValidation;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.TableValidation;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSetMapper extends IMapper<DatasetValue, DataSetVO> {


  /**
   * Map.
   *
   * @param value the value
   *
   * @return the string
   */
  default String map(ObjectId value) {
    return value.toString();
  }


  /**
   * Class to entity.
   *
   * @param model the model
   *
   * @return the dataset value
   */
  @Mapping(source = "tableVO", target = "tableValues")
  @Override
  DatasetValue classToEntity(DataSetVO model);


  /**
   * Entity to class.
   *
   * @param entity the entity
   *
   * @return the data set VO
   */
  @Mapping(source = "tableValues", target = "tableVO")
  @Override
  DataSetVO entityToClass(DatasetValue entity);


  /**
   * Fill ids.
   *
   * @param dataSetVO the data set VO
   * @param dataset the dataset
   */
  @AfterMapping
  default void fillIds(DataSetVO dataSetVO, @MappingTarget DatasetValue dataset) {
    List<TableValue> tableValues = dataset.getTableValues();

    tableValues.parallelStream().forEach(tableValue -> {
      tableValue.setDatasetId(dataset);
      tableValue.getRecords().stream().forEach(record -> {
        record.setTableValue(tableValue);
        record.getFields().stream().forEach(field -> field.setRecord(record));
      });
    });
  }


  /**
   * Fill ids validation.
   *
   * @param dataSetVO the data set VO
   * @param dataset the dataset
   */
  @AfterMapping
  default void fillIdsValidation(DataSetVO dataSetVO, @MappingTarget DatasetValue dataset) {
    List<DatasetValidation> datasetValidations = dataset.getDatasetValidations();
    if (null != datasetValidations) {
      datasetValidations.stream().filter(Objects::nonNull)
          .forEach(datasetValidation -> datasetValidation.setDatasetValue(dataset));
    }
    List<TableValue> tableValues = dataset.getTableValues();
    if (null != tableValues) {
      tableValues.stream().filter(Objects::nonNull).forEach(tableValue -> {
        List<TableValidation> tableValidations = tableValue.getTableValidations();
        if (null != tableValidations) {
          tableValidations.stream().filter(Objects::nonNull)
              .forEach(tableValidation -> tableValidation.setTableValue(tableValue));
        }
        tableValue.getRecords().stream().filter(Objects::nonNull).forEach(record -> {
          if (null != record.getRecordValidations()) {
            record.getRecordValidations().stream().filter(Objects::nonNull)
                .forEach(recordValidation -> recordValidation.setRecordValue(record));
          }
          record.getFields().stream().filter(Objects::nonNull).forEach(field -> {
            if (field.getFieldValidations() != null) {
              field.getFieldValidations().stream()
                  .forEach(fieldValidation -> fieldValidation.setFieldValue(field));
            }
          });
        });
      });
    }
  }
}

