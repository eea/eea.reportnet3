package org.eea.dataset.mapper;

import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.interfaces.vo.dataset.schemas.SimpleFieldSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface FieldSchemaNoRulesMapper.
 */
@Mapper(componentModel = "spring")
public interface SimpleFieldSchemaMapper extends IMapper<FieldSchema, SimpleFieldSchemaVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the field schema VO
   */
  @Override
  @Mapping(source = "headerName", target = "fieldName")
  @Mapping(source = "type", target = "fieldType")
  SimpleFieldSchemaVO entityToClass(FieldSchema entity);

}
