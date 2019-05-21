package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.Dataset;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSetMapper extends IMapper<Dataset, DataSetVO> {

  @Mapping(source = "tableVO", target = "tableValues")
  @Override
  Dataset classToEntity(DataSetVO model);
}

