package org.eea.recordstore.mapper;

import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.mapper.IMapper;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.mapstruct.Mapper;


/**
 * The Interface ProcessMapper.
 */
@Mapper(componentModel = "spring")
public interface ProcessMapper extends IMapper<EEAProcess, ProcessVO> {
}

