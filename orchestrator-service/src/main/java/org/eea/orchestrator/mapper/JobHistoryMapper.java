package org.eea.orchestrator.mapper;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.mapper.IMapper;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.mapstruct.Mapper;

/**
 * The Interface JobHistoryMapper.
 */
@Mapper(componentModel = "spring")
public interface JobHistoryMapper extends IMapper<JobHistory, JobHistoryVO> {
}
