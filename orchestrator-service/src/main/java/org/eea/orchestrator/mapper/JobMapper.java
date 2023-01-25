package org.eea.orchestrator.mapper;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.mapper.IMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.mapstruct.Mapper;

/**
 * The Interface JobMapper.
 */
@Mapper(componentModel = "spring")
public interface JobMapper extends IMapper<Job, JobVO> {

}

