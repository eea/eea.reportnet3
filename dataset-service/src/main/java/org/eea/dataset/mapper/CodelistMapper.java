package org.eea.dataset.mapper;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.eea.interfaces.vo.dataset.CodelistItemVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


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

  /**
   * Fill ids.
   *
   * @param codelist the codelist
   * @param codelistVO the codelist VO
   */
  @AfterMapping
  default void fillIds(Codelist codelist, @MappingTarget CodelistVO codelistVO) {
    if (codelistVO.getItems() != null) {
      List<CodelistItemVO> codelistItemsVO = codelistVO.getItems();
      codelistItemsVO.stream().forEach(item -> item.setCodelistId(codelist.getId()));
    }
  }
}

