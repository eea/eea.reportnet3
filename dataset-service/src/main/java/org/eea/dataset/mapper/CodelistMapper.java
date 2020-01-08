package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface CodelistMapper.
 */
@Mapper(componentModel = "spring")
public interface CodelistMapper extends IMapper<Codelist, CodelistVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the codelist VO
   */
  @Override
  CodelistVO entityToClass(Codelist entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the codelist
   */
  @Override
  Codelist classToEntity(CodelistVO model);

}

