package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface SnapshotMapper.
 */
@Mapper(componentModel = "spring")
public interface ReleaseMapper extends IMapper<Snapshot, ReleaseVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the snapshot VO
   */
  @Override
  @Mapping(source = "reportingDataset.id", target = "datasetId")
  @Mapping(source = "reportingDataset.dataSetName", target = "datasetName")
  @Mapping(source = "release", target = "dcrelease")
  ReleaseVO entityToClass(Snapshot entity);

}
