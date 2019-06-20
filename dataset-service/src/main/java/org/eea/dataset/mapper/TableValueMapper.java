package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The interface DataSetTablesMapper.
 */
@Mapper(componentModel = "spring")
public interface TableValueMapper extends IMapper<TableValue, TableVO> {

}
