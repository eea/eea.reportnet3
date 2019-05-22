package org.eea.dataset.mapper;

import java.util.List;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring", uses = TableNoRecordMapper.class)
public abstract class DataSetNoDataMapper implements IMapper<DatasetValue, DataSetVO> {

  @Mapping(source = "tableVO", target = "tableValues")
  @Override
  public abstract DatasetValue classToEntity(DataSetVO model);


  @Mapping(source = "tableValues", target = "tableVO")
  @Override
  public abstract DataSetVO entityToClass(DatasetValue entity);



  @AfterMapping
  public void fillIds(DataSetVO dataSetVO, @MappingTarget DatasetValue dataset) {
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

