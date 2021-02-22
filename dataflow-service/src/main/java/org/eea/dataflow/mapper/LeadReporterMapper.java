package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.LeadReporter;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface LeadReporterMapper.
 */
@Mapper(componentModel = "spring")
public interface LeadReporterMapper extends IMapper<LeadReporter, LeadReporterVO> {

}
