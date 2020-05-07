package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface RecordValidationMapper.
 */
@Mapper(componentModel = "spring")
public interface RecordValidationMapper extends IMapper<RecordValidation, RecordValidationVO> {

  /**
   * Class to entity.
   *
   * @param vo the vo
   * @return the record validation
   */
  @Override
  @Mapping(source = "recordValue", target = "recordValue", ignore = true)
  RecordValidation classToEntity(RecordValidationVO vo);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the record validation VO
   */
  @Override
  @Mapping(source = "recordValue", target = "recordValue", ignore = true)
  RecordValidationVO entityToClass(RecordValidation entity);
}
