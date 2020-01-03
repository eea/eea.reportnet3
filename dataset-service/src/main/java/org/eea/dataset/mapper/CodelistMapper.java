package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.Codelist;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


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
  @Mapping(source = "category.id", target = "categoryId")
  @Mapping(source = "items.id", target = "itemsId")
  @Override
  CodelistVO entityToClass(Codelist entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the codelist
   */
  @Mapping(source = "categoryId", target = "category.id")
  @Mapping(source = "itemsId", target = "items.id")
  @Override
  Codelist classToEntity(CodelistVO model);

}

