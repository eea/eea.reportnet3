package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The interface RecordMapper.
 */
@Mapper(componentModel = "spring"
)
public interface RecordMapper extends IMapper<RecordValue, RecordVO> {

}

