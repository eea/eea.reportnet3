package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface TaskMapper.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper extends IMapper<Task, TaskVO> {

}
