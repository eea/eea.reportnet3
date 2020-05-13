package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface FieldValidationMapper.
 */
@Mapper(componentModel = "spring")
public interface FieldValidationMapper extends IMapper<FieldValidation, FieldValidationVO> {

  /**
   * Class to entity.
   *
   * @param vo the vo
   * @return the field validation
   */
  @Override
  @Mapping(source = "fieldValue", target = "fieldValue", ignore = true)
  FieldValidation classToEntity(FieldValidationVO vo);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the field validation VO
   */
  @Override
  @Mapping(source = "fieldValue", target = "fieldValue", ignore = true)
  FieldValidationVO entityToClass(FieldValidation entity);

}
