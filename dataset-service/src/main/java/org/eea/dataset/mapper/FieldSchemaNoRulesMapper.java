package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface FieldSchemaNoRulesMapper.
 */
@Mapper(componentModel = "spring")
public interface FieldSchemaNoRulesMapper extends IMapper<FieldSchema, FieldSchemaVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the field schema VO
   */
  @Override
  @Mapping(source = "idRecord", target = "idRecord", ignore = true)
  @Mapping(source = "headerName", target = "name")
  FieldSchemaVO entityToClass(FieldSchema entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the field schema
   */
  @Override
  @Mapping(source = "idRecord", target = "idRecord", ignore = true)
  @Mapping(source = "name", target = "headerName")
  FieldSchema classToEntity(FieldSchemaVO entity);

  /**
   * Fill id record.
   *
   * @param fieldSchema the field schema
   * @param fieldSchemaVO the field schema VO
   */
  @AfterMapping
  default void fillIds(FieldSchema fieldSchema, @MappingTarget FieldSchemaVO fieldSchemaVO) {
    fieldSchemaVO.setId(fieldSchema.getIdFieldSchema().toString());
    fieldSchemaVO.setIdRecord(fieldSchema.getIdRecord().toString());
  }

  /**
   * Fill id record.
   *
   * @param fieldSchemaVO the field schema VO
   * @param fieldSchema the field schema
   */
  @AfterMapping
  default void fillIds(FieldSchemaVO fieldSchemaVO, @MappingTarget FieldSchema fieldSchema) {
    fieldSchema.setIdFieldSchema(new ObjectId(fieldSchemaVO.getId()));
    fieldSchema.setIdRecord(new ObjectId(fieldSchemaVO.getIdRecord()));
  }
}
