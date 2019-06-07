package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValidation;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface DatasetValidationMapper extends IMapper<DatasetValidation, ValidationVO> {

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
  @Mapping(source = "model", target = "validation")
  @Override
  DatasetValidation classToEntity(ValidationVO model);



  /**
   * Class to entity.
   *
   * @param model the model
   * @return the dataset value
   */
  @Mapping(source = "model.validation.id", target = "id")
  @Mapping(source = "model.validation.message", target = "message")
  @Mapping(source = "model.validation.idRule", target = "idRule")
  @Mapping(source = "model.validation.levelError", target = "levelError")
  @Mapping(source = "model.validation.typeEntity", target = "typeEntity")
  @Mapping(source = "model.validation.validationDate", target = "validationDate")
  @Override
  ValidationVO entityToClass(DatasetValidation model);

}

