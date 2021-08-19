package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eea.dataset.mapper.ReferenceDatasetMapper;
import org.eea.dataset.mapper.ReferenceDatasetPublicMapper;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.DataflowReferencedSchema;
import org.eea.dataset.persistence.schemas.repository.DataflowReferencedRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The Class ReferenceDatasetServiceImpl.
 */
@Service
public class ReferenceDatasetServiceImpl implements ReferenceDatasetService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ReferenceDatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /** The reference dataset repository. */
  @Autowired
  private ReferenceDatasetRepository referenceDatasetRepository;


  /** The reference dataset mapper. */
  @Autowired
  private ReferenceDatasetMapper referenceDatasetMapper;

  /** The reference dataset public mapper. */
  @Autowired
  private ReferenceDatasetPublicMapper referenceDatasetPublicMapper;

  /** The dataset schema service. */
  @Autowired
  private DatasetSchemaService datasetSchemaService;


  /** The dataflow referenced repository. */
  @Autowired
  private DataflowReferencedRepository dataflowReferencedRepository;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;


  /**
   * Gets the reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the reference dataset by dataflow id
   */
  @Override
  public List<ReferenceDatasetVO> getReferenceDatasetByDataflowId(Long dataflowId) {
    List<ReferenceDataset> referenceDatasets =
        referenceDatasetRepository.findByDataflowId(dataflowId);
    return referenceDatasetMapper.entityListToClass(referenceDatasets);
  }


  /**
   * Gets the reference dataset public by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the reference dataset public by dataflow
   */
  @Override
  public List<ReferenceDatasetPublicVO> getReferenceDatasetPublicByDataflow(Long dataflowId) {
    List<ReferenceDatasetVO> referenceDatasets = getReferenceDatasetByDataflowId(dataflowId);
    List<ReferenceDatasetVO> references = new ArrayList<>();
    referenceDatasets.stream().forEach(reference -> {
      if (Boolean.TRUE.equals(datasetSchemaService.getDataSchemaById(reference.getDatasetSchema())
          .getAvailableInPublic())) {
        references.add(reference);
      }
    });
    return referenceDatasetPublicMapper.entityListToClass(references);
  }

  /**
   * Gets the dataflows referenced.
   *
   * @param dataflowId the dataflow id
   * @return the dataflows referenced
   */
  @Override
  public Set<DataFlowVO> getDataflowsReferenced(Long dataflowId) {

    Set<DataFlowVO> dataflows = new HashSet<>();
    DataflowReferencedSchema referenced = dataflowReferencedRepository.findByDataflowId(dataflowId);
    if (null != referenced && null != referenced.getReferencedByDataflow()) {
      referenced.getReferencedByDataflow().stream()
          .forEach(r -> dataflows.add(dataflowControllerZuul.getMetabaseById(r)));
    }
    return dataflows;
  }

  /**
   * Update updatable.
   *
   * @param datasetId the dataset id
   * @param updatable the updatable
   */
  public void updateUpdatable(Long datasetId, Boolean updatable) throws EEAException {

    ReferenceDataset referenceDataset = referenceDatasetRepository.findById(datasetId).orElse(null);
    if (null == referenceDataset) {
      throw new EEAException(String.format(EEAErrorMessage.DATASET_NOTFOUND, datasetId));
    }
    referenceDataset.setUpdatable(updatable);
    referenceDatasetRepository.save(referenceDataset);
  }

}
