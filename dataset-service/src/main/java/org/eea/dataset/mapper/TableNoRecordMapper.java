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
public interface TableNoRecordMapper extends IMapper<TableValue, TableVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the table VO
   */
  @Mapping(source = "records", target = "records", ignore = true)
  @Mapping(target = "tableValidations", ignore = true)
  @Override
  TableVO entityToClass(TableValue entity);



}

