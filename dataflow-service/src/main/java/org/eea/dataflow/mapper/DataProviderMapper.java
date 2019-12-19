package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface RepresentativeMapper.
 */
@Mapper(componentModel = "spring")
public interface DataProviderMapper extends IMapper<DataProvider, DataProviderVO> {

  @Override
  @Mapping(source = "group", target = "type")
  DataProvider classToEntity(DataProviderVO model);

  @Override
  @Mapping(source = "type", target = "group")
  DataProviderVO entityToClass(DataProvider entity);
}
