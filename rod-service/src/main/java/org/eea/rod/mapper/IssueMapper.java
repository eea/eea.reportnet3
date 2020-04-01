package org.eea.rod.mapper;

import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.mapper.IMapper;
import org.eea.rod.persistence.domain.Issue;
import org.mapstruct.Mapper;

/**
 * The interface Country mapper.
 */
@Mapper(componentModel = "spring")
public interface IssueMapper extends IMapper<Issue, IssueVO> {

}
