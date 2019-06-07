package org.eea.dataset.mapper;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
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
@Mapper(componentModel = "spring", uses = {DatasetValidationMapper.class,
    TableValidationMapper.class, RecordValidationMapper.class, FieldValidationMapper.class})
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
  @Override
  DatasetValue classToEntity(DataSetVO model);


  /**
   * Entity to class.
   *
   * @param entity the entity
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
    tableValues.stream().forEach(tableValue -> {
      tableValue.setDatasetId(dataset);
      tableValue.getRecords().stream().forEach(record -> {
        record.setTableValue(tableValue);
        record.getFields().stream().forEach(field -> field.setRecord(record));
      });
    });


  }


  //
  // @AfterMapping
  // default void fill(DataSetVO dataSetVO, @MappingTarget DatasetValue dataset) {
  // List<DatasetValidation> dbvalidation = dataset.getDatasetValidations();
  // List<ValidationVO> validationsVODataSet = new ArrayList<>();
  //
  // List<ValidationVO> validationsVO = new ArrayList<>();
  // if (null != dbvalidation) {
  // dbvalidation.stream().forEach(validation -> {
  // ValidationVO validationVo = new ValidationVO();
  // validationVo.setId(validation.getValidation().getId());
  // validationVo.setIdRule(validation.getValidation().getIdRule());
  // validationVo.setLevelError(validation.getValidation().getLevelError());
  // validationVo.setMessage(validation.getValidation().getMessage());
  // validationVo.setTypeEntity(validation.getValidation().getTypeEntity());
  // validationVo.setValidationDate(validation.getValidation().getValidationDate());
  // validationsVODataSet.add(validationVo);
  // });
  // dataset.setDatasetValidations(new ArrayList<>());
  // }
  // List<TableValue> tableValues = dataset.getTableValues();
  // if (null != tableValues) {
  // tableValues.stream().forEach(tableValue -> {
  // List<ValidationVO> validationsVOTable = new ArrayList<>();
  // tableValue.getTableValidations().stream().forEach(validation -> {
  // ValidationVO validationVo = new ValidationVO();
  // validationVo.setId(validation.getValidation().getId());
  // validationVo.setIdRule(validation.getValidation().getIdRule());
  // validationVo.setLevelError(validation.getValidation().getLevelError());
  // validationVo.setMessage(validation.getValidation().getMessage());
  // validationVo.setTypeEntity(validation.getValidation().getTypeEntity());
  // validationVo.setValidationDate(validation.getValidation().getValidationDate());
  // });
  //
  // tableValue.getRecords().stream().forEach(record -> {
  // record.getFields().stream().forEach(field -> {
  //
  // });
  // });
  // });
  // }
  // }
  //



}

