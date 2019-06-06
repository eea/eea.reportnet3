package org.eea.dataset.mapper;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
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
   * @return the string
   */
  default String map(ObjectId value) {
    return value.toString();
  }


  /**
   * Class to entity.
   *
   * @param model the model
   * @return the dataset value
   */
  @Mapping(source = "tableVO", target = "tableValues")
  @Mapping(source = "validations", target = "datasetValidations")
  @Override
  DatasetValue classToEntity(DataSetVO model);


  @Mapping(source = "validations", target = "recordValidations")
  RecordValue classToEntity(RecordVO model);

  @Mapping(source = "validations", target = "tableValidations")
  TableValue classToEntity(TableVO model);

  @Mapping(source = "validations", target = "fieldValidations")
  FieldValue classToEntity(FieldVO model);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data set VO
   */
  @Mapping(source = "tableValues", target = "tableVO")
  @Mapping(source = "datasetValidations", target = "validations")
  @Override
  DataSetVO entityToClass(DatasetValue entity);

  @Mapping(source = "entity.recordValidations", target = "validations")
  RecordVO entityToClass(RecordValue entity);

  @Mapping(source = "entity.tableValidations", target = "validations")
  TableVO entityToClass(TableValue entity);

  @Mapping(source = "entity.fieldValidations", target = "validations")
  FieldVO entityToClass(FieldValue entity);

  /**
   * Fill ids.
   *
   * @param dataSetVO the data set VO
   * @param dataset the dataset
   */
  @AfterMapping
  default void fillIds(DataSetVO dataSetVO, @MappingTarget DatasetValue dataset) {
    List<TableValue> tableValues = dataset.getTableValues();
    tableValues.stream().forEach(tableValue -> {
      tableValue.setDatasetId(dataset);
      tableValue.getRecords().stream().forEach(record -> {
        record.setTableValue(tableValue);
        record.getFields().stream().forEach(field -> field.setRecord(record));
      });
    });

  }


}

