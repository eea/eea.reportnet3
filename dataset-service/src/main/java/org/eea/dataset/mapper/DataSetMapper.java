package org.eea.dataset.mapper;

import org.eea.dataset.persistence.domain.DatasetValue;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataSetMapper extends IMapper<DatasetValue, DataSetVO> {

}

