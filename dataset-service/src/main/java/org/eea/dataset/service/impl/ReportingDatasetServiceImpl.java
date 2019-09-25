package org.eea.dataset.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataset.mapper.ReportingDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
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

  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  @Autowired
  private ReportingDatasetMapper reportingDatasetMapper;

  @Autowired
  private SnapshotRepository snapshotRepository;


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

    isReleased(datasetsVO);

    return datasetsVO;
  }

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


}
