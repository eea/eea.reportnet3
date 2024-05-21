package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DatasetTableMapper.
 */
@Mapper(componentModel = "spring")
public interface DatasetTableMapper extends IMapper<DatasetTable, DatasetTableVO>{

}

