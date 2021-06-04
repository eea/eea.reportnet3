package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eea.dataset.mapper.ReferenceDatasetMapper;
import org.eea.dataset.mapper.ReferenceDatasetPublicMapper;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.ReferenceDatasetService;
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

  /** The pk catalogue repository. */
  @Autowired
  private PkCatalogueRepository pkCatalogueRepository;

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
    List<PkCatalogueSchema> catalogues = pkCatalogueRepository.findByDataflowId(dataflowId);
    for (PkCatalogueSchema catalogue : catalogues) {
      if (null != catalogue.getReferencedByDataflow()
          && !catalogue.getReferencedByDataflow().isEmpty()) {
        for (Long dataflowReferenced : catalogue.getReferencedByDataflow()) {
          dataflows.add(dataflowControllerZuul.getMetabaseById(dataflowReferenced));
        }
      }
    }
    return dataflows;
  }
}
