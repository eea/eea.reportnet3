package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface WebFormMapper.
 */
@Mapper(componentModel = "spring")
public interface WebformMetabaseMapper extends IMapper<WebformMetabase, WebformMetabaseVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the web form VO
   */
  @Override
  WebformMetabaseVO entityToClass(WebformMetabase entity);


  /**
   * Class to entity.
   *
   * @param vo the vo
   * @return the web form
   */
  @Override
  WebformMetabase classToEntity(WebformMetabaseVO vo);

  /**
   * Map.
   *
   * @param value the value
   * @return the string
   */
  default String map(ObjectId value) {
    return value.toString();
  }

  /**
   * Map.
   *
   * @param value the value
   * @return the object id
   */
  default ObjectId map(String value) {
    return new ObjectId(value);
  }


}

