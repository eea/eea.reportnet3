package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.Validation;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The interface RecordMapper.
 */
@Mapper(componentModel = "spring")
public interface ValidationMapper extends IMapper<Validation, ValidationVO> {

}

