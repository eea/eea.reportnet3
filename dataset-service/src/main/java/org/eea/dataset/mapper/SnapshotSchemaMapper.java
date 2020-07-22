package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.SnapshotSchema;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



/**
 * The Interface SnapshotSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface SnapshotSchemaMapper extends IMapper<SnapshotSchema, SnapshotVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the snapshot VO
   */
  @Override
  @Mapping(source = "designDataset.id", target = "datasetId")
  SnapshotVO entityToClass(SnapshotSchema entity);

}
