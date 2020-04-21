package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.interfaces.vo.dataset.ETLRecordVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface ETLRecordMapper.
 */
@Mapper(componentModel = "spring")
public interface ETLRecordMapper extends IMapper<RecordValue, ETLRecordVO> {

  /**
   * Class to entity.
   *
   * @param eltRecordVO the elt record VO
   * @return the record value
   */
  @Override
  RecordValue classToEntity(ETLRecordVO eltRecordVO);

  /**
   * Entity to class.
   *
   * @param recordValue the record value
   * @return the ETL record VO
   */
  @Override
  ETLRecordVO entityToClass(RecordValue recordValue);
}
