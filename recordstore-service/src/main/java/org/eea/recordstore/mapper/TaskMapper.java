package org.eea.recordstore.mapper;

import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.mapper.IMapper;
import org.eea.recordstore.persistence.domain.Task;
import org.mapstruct.Mapper;

/**
 * The Interface TaskMapper.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper extends IMapper<Task, TaskVO> {

}
