package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.CodelistItem;
import org.eea.interfaces.vo.dataset.CodelistItemVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface CodelistItemMapper.
 */
@Mapper(componentModel = "spring")
public interface CodelistItemMapper extends IMapper<CodelistItem, CodelistItemVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the codelist VO
   */
  @Override
  CodelistItemVO entityToClass(CodelistItem entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the codelist
   */
  @Override
  CodelistItem classToEntity(CodelistItemVO model);

}

