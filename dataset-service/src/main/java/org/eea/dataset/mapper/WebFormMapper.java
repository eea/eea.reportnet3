package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebForm;
import org.eea.interfaces.vo.dataset.schemas.WebFormVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface WebFormMapper.
 */
@Mapper(componentModel = "spring")
public interface WebFormMapper extends IMapper<WebForm, WebFormVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the web form VO
   */
  @Override
  WebFormVO entityToClass(WebForm entity);


  /**
   * Class to entity.
   *
   * @param vo the vo
   * @return the web form
   */
  @Override
  WebForm classToEntity(WebFormVO vo);

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

