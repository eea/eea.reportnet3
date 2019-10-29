package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FieldSchemaNoRulesMapper extends IMapper<FieldSchema, FieldSchemaVO> {

  @Override
  @Mapping(source = "idRecord", target = "idRecord", ignore = true)
  @Mapping(source = "ruleField", target = "ruleField", ignore = true)
  @Mapping(source = "headerName", target = "name")
  FieldSchemaVO entityToClass(FieldSchema entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the table schema
   */
  @Override
  @Mapping(source = "idRecord", target = "idRecord", ignore = true)
  @Mapping(source = "ruleField", target = "ruleField", ignore = true)
  @Mapping(source = "name", target = "headerName")
  FieldSchema classToEntity(FieldSchemaVO entity);

  @AfterMapping
  default void fillIdRecord(FieldSchema fieldSchema, @MappingTarget FieldSchemaVO fieldSchemaVO) {
    fieldSchemaVO.setIdRecord(fieldSchema.getIdRecord().toString());
  }

  @AfterMapping
  default void fillIdRecord(FieldSchemaVO fieldSchemaVO, @MappingTarget FieldSchema fieldSchema) {
    fieldSchema.setIdRecord(new ObjectId(fieldSchemaVO.getIdRecord()));
  }
}
