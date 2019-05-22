package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public abstract class RecordMapper implements IMapper<RecordValue, RecordVO> {



}

