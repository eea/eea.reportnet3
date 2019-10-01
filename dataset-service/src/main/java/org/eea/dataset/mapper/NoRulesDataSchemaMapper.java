package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface NoRulesDataSchemaMapper extends IMapper<DataSetSchema, DataSetSchemaVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   *
   * @return the data set schema VO
   */
  @Override
  @Mapping(source = "ruleDataSet", target = "ruleDataSet", ignore = true)
  DataSetSchemaVO entityToClass(DataSetSchema entity);


  /**
   * Map.
   *
   * @param value the value
   *
   * @return the string
   */
  default String map(ObjectId value) {
    return value.toString();
  }

  /**
   * Map.
   *
   * @param value the value
   *
   * @return the object id
   */
  default ObjectId map(String value) {
    return new ObjectId(value);
  }

  /**
   * Entity to class.
   *
   * @param model the model
   *
   * @return the field schema VO
   */
  @Mapping(source = "headerName", target = "name")
  @Mapping(source = "idFieldSchema", target = "id")
  @Mapping(source = "ruleField", target = "ruleField", ignore = true)
  FieldSchemaVO entityToClass(FieldSchema model);

  /**
   * Entity to class record schema vo.
   *
   * @param model the model
   *
   * @return the record schema vo
   */
  @Mapping(source = "ruleRecord", target = "ruleRecord", ignore = true)
  RecordSchemaVO entityToClass(RecordSchema model);

  /**
   * Entity to class table schema vo.
   *
   * @param model the model
   *
   * @return the table schema vo
   */
  @Mapping(source = "ruleTable", target = "ruleTable", ignore = true)
  TableSchemaVO entityToClass(TableSchema model);

}
