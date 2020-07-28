package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface SnapshotMapper.
 */
@Mapper(componentModel = "spring")
public interface SnapshotMapper extends IMapper<Snapshot, SnapshotVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the snapshot VO
   */
  @Override
  @Mapping(source = "reportingDataset.id", target = "datasetId")
  SnapshotVO entityToClass(Snapshot entity);
}
