package org.eea.dataflow.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The Class IntegrationServiceImpl.
 */
@Service("integrationService")
public class IntegrationServiceImpl implements IntegrationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(IntegrationServiceImpl.class);

  /** The Constant FILE_EXTENSION: {@value}. */
  private static final String FILE_EXTENSION = "fileExtension";

  /** The Constant DATASETSCHEMAID: {@value}. */
  private static final String DATASETSCHEMAID = "datasetSchemaId";

  /** The Constant DATAFLOW_ID: {@value}. */
  private static final String DATAFLOW_ID = "dataflowId";

  /** The Constant DATASET_ID: {@value}. */
  private static final String DATASET_ID = "datasetId";

  /** The Constant PROCESS_NAME: {@value}. */
  private static final String PROCESS_NAME = "processName";

  /** The Constant REPOSITORY: {@value}. */
  private static final String REPOSITORY = "repository";

  /** The crud manager factory. */
  @Autowired
  private CrudManagerFactory crudManagerFactory;

  /** The FME integration executor factory. */
  @Autowired
  private IntegrationExecutorFactory integrationExecutorFactory;

  /** The eu dataset controller zuul. */
  @Autowired
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * Creates the integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void createIntegration(IntegrationVO integrationVO) throws EEAException {
    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.create(integrationVO);
  }

  /**
   * Delete integration.
   *
   * @param integrationId the integration id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void deleteIntegration(Long integrationId) throws EEAException {
    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.delete(integrationId);
  }

  /**
   * Update integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void updateIntegration(IntegrationVO integrationVO) throws EEAException {
    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.update(integrationVO);
  }

  /**
   * Gets the all integrations by criteria.
   *
   * @param integrationVO the integration VO
   * @return the all integrations by criteria
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public List<IntegrationVO> getAllIntegrationsByCriteria(IntegrationVO integrationVO)
      throws EEAException {
    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    return crudManager.get(integrationVO);
  }

  /**
   * Gets only extensions and operations.
   *
   * @param integrationVOList the integration VO list
   * @return the only extensions and operations
   */
  @Override
  public List<IntegrationVO> getOnlyExtensionsAndOperations(List<IntegrationVO> integrationVOList) {
    // Remove all data except operation and fileExtension
    List<IntegrationVO> newIntegrationVOList = new ArrayList<>();
    integrationVOList.stream().forEach(integration -> {
      IntegrationVO integrationVOAux = new IntegrationVO();
      Map<String, String> internalParameters = new HashMap<>();
      internalParameters.put(FILE_EXTENSION,
          integration.getInternalParameters().get(FILE_EXTENSION));
      internalParameters.put(DATASETSCHEMAID,
          integration.getInternalParameters().get(DATASETSCHEMAID));
      integrationVOAux.setOperation(integration.getOperation());
      integrationVOAux.setInternalParameters(internalParameters);
      newIntegrationVOList.add(integrationVOAux);
    });
    return newIntegrationVOList;
  }

  /**
   * Copy integrations.
   *
   * @param dataflowIdDestination the dataflow id destination
   * @param originDatasetSchemaIds the origin dataset schema ids
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void copyIntegrations(Long dataflowIdDestination, List<String> originDatasetSchemaIds,
      Map<String, String> dictionaryOriginTargetObjectId) throws EEAException {
    for (String originDatasetSchemaId : originDatasetSchemaIds) {
      IntegrationVO integrationCriteria = new IntegrationVO();
      integrationCriteria.getInternalParameters().put(DATASETSCHEMAID, originDatasetSchemaId);
      List<IntegrationVO> integrations = getAllIntegrationsByCriteria(integrationCriteria);
      for (IntegrationVO integration : integrations) {
        // we've got the origin integrations. We intend to change the dataflow and the
        // datasetSchemaId
        // parameters and save all as new integrations of the copied design datasets
        LOG.info(
            "There are integrations to be copied into the datasetSchemaId {} in the dataflowId {}",
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId), dataflowIdDestination);
        integration.getInternalParameters().put(DATASETSCHEMAID,
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId));
        integration.getInternalParameters().put("dataflowId", dataflowIdDestination.toString());
        createIntegration(integration);
      }
    }
  }

  /**
   * Execute EU dataset export.
   *
   * @param dataflowId the dataflow id
   * @return the list
   * @throws EEAException the EEA exception
   */
  @Override
  public List<ExecutionResultVO> executeEUDatasetExport(Long dataflowId) throws EEAException {

    addLock(dataflowId);
    IntegrationToolTypeEnum integrationToolTypeEnum = IntegrationToolTypeEnum.FME;
    IntegrationOperationTypeEnum integrationOperationTypeEnum =
        IntegrationOperationTypeEnum.EXPORT_EU_DATASET;
    IntegrationVO integration = new IntegrationVO();
    integration.setTool(integrationToolTypeEnum);
    integration.setOperation(integrationOperationTypeEnum);

    List<EUDatasetVO> euDatasets = euDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId);

    List<ExecutionResultVO> resultList = new ArrayList<>();

    euDatasets.stream().forEach(
        dataset -> resultList.add(integrationExecutorFactory.getExecutor(integrationToolTypeEnum)
            .execute(integrationOperationTypeEnum, null, dataset.getId(), integration)));

    releaseLock(dataflowId);
    return resultList;
  }

  /**
   * Creates the default integration.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void createDefaultIntegration(Long dataflowId, Long datasetId, String datasetSchemaId)
      throws EEAException {
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(DATAFLOW_ID, dataflowId.toString());
    internalParameters.put(DATASET_ID, datasetId.toString());
    internalParameters.put(DATASETSCHEMAID, datasetSchemaId);
    internalParameters.put(REPOSITORY, "ReportNetTesting");
    internalParameters.put(PROCESS_NAME, "Export_EU_dataset.fmw");

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setDescription("Export EU Dataset");
    integrationVO.setInternalParameters(internalParameters);
    integrationVO.setName("Export EU Dataset");
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    integrationVO.setTool(IntegrationToolTypeEnum.FME);

    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.create(integrationVO);
  }

  /**
   * Release lock.
   *
   * @param dataflowId the dataflow id
   */
  private void releaseLock(Long dataflowId) {
    // Remove lock to the operation export from EU dataset
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.EXPORT_EU_DATASET.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    // Remove lock to the operation copy data to EU dataset
    List<Object> criteriaCopy = new ArrayList<>();
    criteriaCopy.add(LockSignature.POPULATE_EU_DATASET.getValue());
    criteriaCopy.add(dataflowId);
    lockService.removeLockByCriteria(criteriaCopy);
  }

  /**
   * Adds the lock.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void addLock(Long dataflowId) throws EEAException {
    // Lock to avoid export EUDataset while is copying data
    Map<String, Object> mapCriteriaExport = new HashMap<>();
    mapCriteriaExport.put("signature", LockSignature.POPULATE_EU_DATASET.getValue());
    mapCriteriaExport.put("dataflowId", dataflowId);
    lockService.createLock(new Timestamp(System.currentTimeMillis()),
        SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
        mapCriteriaExport);
  }
}
