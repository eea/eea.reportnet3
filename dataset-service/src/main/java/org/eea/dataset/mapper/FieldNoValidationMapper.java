package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface FieldNoValidationMapper.
 */
@Mapper(componentModel = "spring")
public interface FieldNoValidationMapper extends IMapper<FieldValue, FieldVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the record VO
   */
  @Mapping(target = "fieldValidations", ignore = true)
  @Override
  FieldVO entityToClass(FieldValue entity);

}
