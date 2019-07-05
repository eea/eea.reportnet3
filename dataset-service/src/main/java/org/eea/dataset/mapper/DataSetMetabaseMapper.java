package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DataSetMetabaseMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSetMetabaseMapper extends IMapper<DataSetMetabase, DataSetMetabaseVO> {

}
