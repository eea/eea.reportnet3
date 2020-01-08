package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;



/**
 * The Interface DataCollectionMapper.
 */
@Mapper(componentModel = "spring")
public interface DataCollectionMapper extends IMapper<DataCollection, DataCollectionVO> {

}
