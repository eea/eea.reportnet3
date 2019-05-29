package org.eea.dataset.mapper;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.Dataset;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public abstract class DataSetMapper implements IMapper<Dataset, DataSetVO> {

  String map(ObjectId value) {
    return value.toString();
  }
  
  public abstract DataSetSchemaVO entityToClass(DataSetSchema entity);
  
  @Mapping(source = "tableVO", target = "tableValues")
  @Override
  public abstract Dataset classToEntity(DataSetVO model);


  @Mapping(source = "tableValues", target = "tableVO")
  @Override
  public abstract DataSetVO entityToClass(Dataset entity);

  public abstract TableValue classToEntity(TableVO model);
  
 


  @AfterMapping
  public void fillIds(DataSetVO dataSetVO, @MappingTarget Dataset dataset) {
    List<TableValue> tableValues = dataset.getTableValues();
    tableValues.stream().forEach(tableValue -> {
      tableValue.setDatasetId(dataset);
      tableValue.getRecords().stream().forEach(record -> {
        record.setTableValue(tableValue);
        record.getFields().stream().forEach(field -> field.setRecord(record));
      });
    });

  }


}

