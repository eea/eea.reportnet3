package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.CodelistCategory;
import org.eea.interfaces.vo.dataset.CodelistCategoryFullVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface CodelistCategoryMapper.
 *
 * @deprecated (unused)
 */
@Mapper(componentModel = "spring")
@Deprecated
public interface CodelistCategoryFullMapper
    extends IMapper<CodelistCategory, CodelistCategoryFullVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the codelist VO
   */
  @Override
  CodelistCategoryFullVO entityToClass(CodelistCategory entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the codelist
   */
  @Override
  CodelistCategory classToEntity(CodelistCategoryFullVO model);

}

