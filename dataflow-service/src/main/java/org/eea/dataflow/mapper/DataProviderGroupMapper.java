package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.interfaces.vo.dataflow.DataProviderGroupVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface DataProviderGroupMapper.
 */
@Mapper(componentModel = "spring")
public interface DataProviderGroupMapper extends IMapper<DataProviderGroup, DataProviderGroupVO> {
}
