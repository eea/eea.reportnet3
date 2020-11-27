package org.eea.dataset.service.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eea.dataset.mapper.ReportingDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
        datasetsVO.stream().map(ReportingDatasetVO::getDatasetSchema).collect(Collectors.toList());
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
      List<Snapshot> resultSnapshots =
          snapshotRepository.findByReportingDatasetAndRelease(collection, true);
      List<Long> result = resultSnapshots.stream().map(Snapshot::getReportingDataset)
          .map(DataSetMetabase::getId).collect(Collectors.toList());
      for (ReportingDatasetVO dataset : datasetsVO) {
        if (result != null && !result.isEmpty() && dataset != null && dataset.getId() != null) {

          Boolean isReleased = result.contains(dataset.getId());
          dataset.setIsReleased(isReleased);
          // set the date of the release
          if (dataset.getIsReleased() && !resultSnapshots.stream()
              .filter(s -> s.getReportingDataset().getId().equals(dataset.getId()))
              .collect(Collectors.toList()).isEmpty()) {
            dataset.setDateReleased(resultSnapshots.stream()
                .filter(s -> s.getReportingDataset().getId().equals(dataset.getId()))
                .collect(Collectors.toList()).get(0).getDateReleased());
          }
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


  /**
   * Update reporting dataset metabase.
   *
   * @param reportingVO the reporting VO
   */
  @Override
  @Transactional
  public void updateReportingDatasetMetabase(ReportingDatasetVO reportingVO) {
    ReportingDataset reporting =
        reportingDatasetRepository.findById(reportingVO.getId()).orElse(null);
    if (reporting != null) {
      // Map the VO fields that are informed into the entity
      setEntityProperty(reportingVO, reporting);
      reportingDatasetRepository.save(reporting);
    }
  }


  /**
   * Sets the entity property.
   *
   * @param objectOrigin the object origin
   * @param objectTarget the object target
   */
  private void setEntityProperty(Object objectOrigin, Object objectTarget) {
    Class<?> clazzOrigin = objectOrigin.getClass();
    Class<?> clazzTarget = objectTarget.getClass();

    List<Field> fieldsOrigin = FieldUtils.getAllFieldsList(clazzOrigin);
    List<Field> fieldsTarget = FieldUtils.getAllFieldsList(clazzTarget);
    fieldsTarget.stream().forEach(f -> {
      Field matched = fieldsOrigin.stream().filter(d -> d.getName().equals(f.getName())).findFirst()
          .orElse(null);
      if (matched != null) {
        matched.setAccessible(true);
        try {
          if (matched != null && matched.get(objectOrigin) != null) {
            f.setAccessible(true);
            f.set(objectTarget, matched.get(objectOrigin));
          }
        } catch (IllegalArgumentException | IllegalAccessException e) {
          LOG_ERROR.error("Error mapping the entity {} from the VO {}", clazzTarget.getName(),
              clazzOrigin.getName(), e);
        }
      }
    });
  }



}
