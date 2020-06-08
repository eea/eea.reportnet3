package org.eea.dataflow.integration.fme.mapper;

import org.eea.dataflow.integration.fme.domain.FMEStatus;
import org.eea.interfaces.vo.dataflow.FMEStatusVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FMEStatusMapper extends IMapper<FMEStatus, FMEStatusVO> {

}
