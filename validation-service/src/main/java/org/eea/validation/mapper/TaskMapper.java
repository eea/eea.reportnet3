package org.eea.validation.mapper;

import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.mapstruct.Mapper;

/**
 * The Interface TaskMapper.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper extends IMapper<Task, TaskVO> {

}
