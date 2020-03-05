package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.CodelistCategory;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface CodelistCategoryMapper.
 *
 * @deprecated (unused)
 */
@Mapper(componentModel = "spring")
@Deprecated
public interface CodelistCategoryMapper extends IMapper<CodelistCategory, CodelistCategoryVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the codelist VO
   */
  @Override
  CodelistCategoryVO entityToClass(CodelistCategory entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the codelist
   */
  @Override
  CodelistCategory classToEntity(CodelistCategoryVO model);

}

