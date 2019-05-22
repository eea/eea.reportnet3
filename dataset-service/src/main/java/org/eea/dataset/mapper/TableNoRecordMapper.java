package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public abstract class TableNoRecordMapper implements IMapper<TableValue, TableVO> {



  @Mapping(source = "records", target = "records", ignore = true)
  @Override
  public abstract TableVO entityToClass(TableValue entity);



}

