package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The Interface SnapshotMapper.
 */
@Mapper(componentModel = "spring")
public abstract class ReleaseMapper implements IMapper<Snapshot, ReleaseVO> {

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the snapshot VO
   */
  @Override
  @Mapping(source = "reportingDataset.id", target = "datasetId")
  @Mapping(source = "reportingDataset.dataSetName", target = "datasetName")
  @Mapping(source = "dcReleased", target = "dcrelease")
  @Mapping(source = "euReleased", target = "eurelease")
  public abstract ReleaseVO entityToClass(Snapshot entity);


  /**
   * After mapping.
   *
   * @param snapshot the snapshot
   * @param releaseVO the release VO
   */
  @AfterMapping
  public void afterMapping(Snapshot snapshot, @MappingTarget ReleaseVO releaseVO) {
    if (snapshot.getReportingDataset().getDataProviderId() != null) {
      releaseVO.setCountryCode(representativeControllerZuul
          .findDataProviderById(snapshot.getReportingDataset().getDataProviderId()).getCode());
    }
  }
}
