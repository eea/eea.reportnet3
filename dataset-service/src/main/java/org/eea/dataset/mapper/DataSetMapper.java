package org.eea.dataset.mapper;

import org.eea.dataset.persistence.domain.Dataset;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSetMapper extends IMapper<Dataset, DataSetVO> {

}

