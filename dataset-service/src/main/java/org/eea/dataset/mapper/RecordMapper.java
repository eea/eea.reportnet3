package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


/**
 * The interface RecordMapper.
 */
@Mapper(componentModel = "spring")
public interface RecordMapper extends IMapper<RecordValue, RecordVO> {

  /**
   * Fill ids.
   *
   * @param recordVO the record VO
   * @param recordValue the record value
   */
  @AfterMapping
  default void fillIds(RecordVO recordVO, @MappingTarget RecordValue recordValue) {
    recordValue.getFields().stream().forEach(field -> field.setRecord(recordValue));
  }
}

