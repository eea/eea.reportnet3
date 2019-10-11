package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Document;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * The Interface DataflowMapper.
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper extends IMapper<Document, DocumentVO> {


  /**
   * Fill category.
   *
   * @param document the document
   * @param documentVO the document VO
   */
  @AfterMapping
  default void fillCategory(Document document, @MappingTarget DocumentVO documentVO) {
    documentVO.setDataflowId(document.getDataflow().getId());
  }
}
