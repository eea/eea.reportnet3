package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.InternalProcess;
import org.eea.interfaces.vo.dataset.InternalProcessVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface InternalProcessMapper.
 */
@Mapper(componentModel = "spring")
public interface InternalProcessMapper extends IMapper<InternalProcess, InternalProcessVO> {

}
