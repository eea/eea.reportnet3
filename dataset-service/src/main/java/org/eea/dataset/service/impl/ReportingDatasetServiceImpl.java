package org.eea.dataset.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataset.mapper.ReportingDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DatasetMetabaseServiceImpl.
 */
@Service("reportingDatasetService")
public class ReportingDatasetServiceImpl implements ReportingDatasetService {

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The reporting dataset mapper. */
  @Autowired
  private ReportingDatasetMapper reportingDatasetMapper;

  /** The snapshot repository. */
  @Autowired
  private SnapshotRepository snapshotRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;


  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  @Override
  public List<ReportingDatasetVO> getDataSetIdByDataflowId(Long idFlow) {

    List<ReportingDataset> datasets = reportingDatasetRepository.findByDataflowId(idFlow);

    List<ReportingDatasetVO> datasetsVO = reportingDatasetMapper.entityListToClass(datasets);

    // Check if dataset is released
    isReleased(datasetsVO);

    getDatasetSchemaNames(datasetsVO);

    return datasetsVO;
  }


  /**
   * Gets the dataset schema names.
   *
   * @param datasetsVO the datasets VO
   * @return the dataset schema names
   */
  private void getDatasetSchemaNames(List<ReportingDatasetVO> datasetsVO) {
    List<String> datasetsSchemas =
        datasetsVO.stream().map(dataset -> dataset.getDatasetSchema()).collect(Collectors.toList());
    if (!datasetsSchemas.isEmpty()) {
      List<DesignDataset> resultList =
          designDatasetRepository.findbyDatasetSchemaList(datasetsSchemas);
      datasetsVO.stream().forEach(dataset -> resultList.stream().forEach(design -> {
        if (design.getDatasetSchema().equals(dataset.getDatasetSchema())) {
          dataset.setNameDatasetSchema(design.getDataSetName());
        }
      }));
    }
  }


  /**
   * Checks if is released.
   *
   * @param datasetsVO the datasets VO
   */
  private void isReleased(List<ReportingDatasetVO> datasetsVO) {
    if (datasetsVO != null && !datasetsVO.isEmpty()) {
      List<Long> collection =
          datasetsVO.stream().map(ReportingDatasetVO::getId).collect(Collectors.toList());
      List<Long> result = snapshotRepository.findByReportingDatasetAndRelease(collection);
      for (ReportingDatasetVO dataset : datasetsVO) {
        if (result != null && dataset != null && dataset.getId() != null) {
          Boolean isReleased = result.contains(dataset.getId());
          dataset.setIsReleased(isReleased);
        }
      }
    }
  }

  /**
   * Gets the data set id by dataflow id and schema id.
   *
   * @param idDataflow the id dataflow
   * @param schemaId the schema id
   * @return the data set id by dataflow id and schema id
   */
  @Override
  public List<ReportingDatasetVO> getDataSetIdBySchemaId(String schemaId) {
    List<ReportingDataset> datasets = reportingDatasetRepository.findByDatasetSchema(schemaId);

    List<ReportingDatasetVO> datasetsVO = reportingDatasetMapper.entityListToClass(datasets);

    // Check if dataset is released
    isReleased(datasetsVO);

    return datasetsVO;
  }


}
