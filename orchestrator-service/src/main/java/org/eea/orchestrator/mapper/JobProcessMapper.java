package org.eea.orchestrator.mapper;

import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.mapper.IMapper;
import org.eea.orchestrator.persistence.domain.JobProcess;
import org.mapstruct.Mapper;

/**
 * The Interface JobProcessMapper.
 */
@Mapper(componentModel = "spring")
public interface JobProcessMapper extends IMapper<JobProcess, JobProcessVO> {

}

