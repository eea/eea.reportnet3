package org.eea.dataset.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The Class ReleaseMapper.
 */
@Mapper(componentModel = "spring")
public abstract class ReleaseMapper implements IMapper<Snapshot, ReleaseVO> {

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the snapshot VO
   */
  @Override
  @Mapping(source = "reportingDataset.id", target = "datasetId")
  @Mapping(source = "reportingDataset.datasetSchema", target = "datasetName")
  @Mapping(source = "dcReleased", target = "dcrelease")
  @Mapping(source = "euReleased", target = "eurelease")
  public abstract ReleaseVO entityToClass(Snapshot entity);


  /**
   * After mapping to fill the country code.
   *
   * @param snapshot the snapshot
   * @param releaseVO the release VO
   */
  @AfterMapping
  public void afterMapping(Snapshot snapshot, @MappingTarget ReleaseVO releaseVO) {
    if (snapshot.getReportingDataset().getDataProviderId() != null) {
      releaseVO.setDataProviderCode(representativeControllerZuul
          .findDataProviderById(snapshot.getReportingDataset().getDataProviderId()).getCode());
      List<RepresentativeVO> representatives =
          representativeControllerZuul.findRepresentativesByIdDataFlow(snapshot.getDataflowId());
      representatives.stream().forEach(representative -> {
        releaseVO.setRestrictFromPublic(representative.isRestrictFromPublic());
      });
    }
  }

  /**
   * After mapping for replace the schemaid by hits name.
   *
   * @param snapshot the snapshot
   * @param releaseVO the release VO
   */
  @AfterMapping
  public void afterMapping(List<Snapshot> snapshot, @MappingTarget List<ReleaseVO> releaseVO) {
    List<String> datasetsSchemas =
        releaseVO.stream().map(ReleaseVO::getDatasetName).collect(Collectors.toList());
    if (!datasetsSchemas.isEmpty()) {
      List<DesignDataset> resultList =
          designDatasetRepository.findbyDatasetSchemaList(datasetsSchemas);
      releaseVO.stream().forEach(release -> resultList.stream().forEach(design -> {
        if (design.getDatasetSchema().equals(release.getDatasetName())) {
          release.setDatasetName(design.getDataSetName());
        }
      }));
    }
  }
}
