package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.interfaces.vo.dataset.ETLFieldVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface ETLFieldMapper.
 */
@Mapper(componentModel = "spring")
public interface ETLFieldMapper extends IMapper<FieldValue, ETLFieldVO> {

  /**
   * Class to entity.
   *
   * @param etlFieldVO the etl field VO
   * @return the field value
   */
  @Override
  FieldValue classToEntity(ETLFieldVO etlFieldVO);

  /**
   * Entity to class.
   *
   * @param fieldValue the field value
   * @return the ETL field VO
   */
  @Override
  ETLFieldVO entityToClass(FieldValue fieldValue);
}
