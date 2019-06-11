package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecordValidationMapper extends IMapper<RecordValidation, RecordValidationVO> {


  @Mapping(source = "recordValue", target = "recordValue", ignore = true)
  RecordValidation classToEntity(RecordValidationVO vo);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the t
   */
  @Mapping(source = "recordValue", target = "recordValue", ignore = true)
  RecordValidationVO entityToClass(RecordValidation entity);

}
