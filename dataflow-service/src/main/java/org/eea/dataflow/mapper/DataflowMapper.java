package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataflowMapper extends IMapper<Dataflow, DataFlowVO> {

}
