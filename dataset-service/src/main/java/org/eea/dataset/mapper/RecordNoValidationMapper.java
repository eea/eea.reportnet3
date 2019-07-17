package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.mapper.IMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface RecordNoValidationMapper.
 */
@Mapper(componentModel = "spring", uses = FieldNoValidationMapper.class,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RecordNoValidationMapper extends IMapper<RecordValue, RecordVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the record VO
   */
  @Mapping(target = "recordValidations", ignore = true)
  @Override
  RecordVO entityToClass(RecordValue entity);

}
