package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.TableValidation;
import org.eea.interfaces.vo.dataset.TableValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface TableValidationMapper.
 */
@Mapper(componentModel = "spring")
public interface TableValidationMapper extends IMapper<TableValidation, TableValidationVO> {

  /** The Constant TABLE_VALUE. */
  String TABLE_VALUE = "tableValue";

  /**
   * Class to entity.
   *
   * @param vo the vo
   * @return the table validation
   */
  @Override
  @Mapping(source = TABLE_VALUE, target = TABLE_VALUE, ignore = true)
  TableValidation classToEntity(TableValidationVO vo);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the t
   */
  @Override
  @Mapping(source = TABLE_VALUE, target = TABLE_VALUE, ignore = true)
  TableValidationVO entityToClass(TableValidation entity);

}
