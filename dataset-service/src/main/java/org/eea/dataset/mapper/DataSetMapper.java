package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.Dataset;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.Record;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSetMapper extends IMapper<Dataset, DataSetVO> {

  @Mapping(source = "tableVO", target = "tableValues")
  @Override
  public Dataset classToEntity(DataSetVO model);

  @Mapping(source = "tableValues", target = "tableVO")
  @Override
  public DataSetVO entityToClass(Dataset entity);

  public TableValue classToEntity(TableVO model);

  public TableVO entityToClass(TableValue entity);

  public Record classToEntity(RecordVO model);

  public RecordVO entityToClass(Record entity);

  public FieldValue classToEntity(FieldVO model);

  public FieldVO entityToClass(FieldValue entity);
}

