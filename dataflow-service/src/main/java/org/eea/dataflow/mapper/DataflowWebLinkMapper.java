package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DataflowMapper.
 */
@Mapper(componentModel = "spring")
public interface DataflowWebLinkMapper extends IMapper<Weblink, WeblinkVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data flow VO
   */
  @Override
  WeblinkVO entityToClass(Weblink entity);


}
