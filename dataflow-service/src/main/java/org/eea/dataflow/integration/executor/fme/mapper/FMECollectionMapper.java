package org.eea.dataflow.integration.executor.fme.mapper;

import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.interfaces.vo.dataflow.FMECollectionVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FMECollectionMapper extends IMapper<FMECollection, FMECollectionVO> {

}
