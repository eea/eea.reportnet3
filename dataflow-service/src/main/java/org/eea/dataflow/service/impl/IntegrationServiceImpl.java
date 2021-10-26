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
import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class IntegrationServiceImpl.
 */
@Service("integrationService")
public class IntegrationServiceImpl implements IntegrationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(IntegrationServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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

  /** The integration repository. */
  @Autowired
  private IntegrationRepository integrationRepository;

  /** The integration mapper. */
  @Autowired
  private IntegrationMapper integrationMapper;

  /** The dataset controller zuul. */
  @Autowired
  private DataSetControllerZuul datasetControllerZuul;



  /**
   * Creates the integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void createIntegration(IntegrationVO integrationVO) throws EEAException {

    if (IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(integrationVO.getOperation())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_CREATION);
    }

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

    IntegrationOperationTypeEnum operation = integrationRepository.findOperationById(integrationId);
    if (IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(operation)) {
      throw new EEAException(EEAErrorMessage.FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_DELETION);
    }

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
      internalParameters.put(IntegrationParams.FILE_EXTENSION,
          integration.getInternalParameters().get(IntegrationParams.FILE_EXTENSION));
      internalParameters.put(IntegrationParams.DATASET_SCHEMA_ID,
          integration.getInternalParameters().get(IntegrationParams.DATASET_SCHEMA_ID));
      integrationVOAux.setOperation(integration.getOperation());
      integrationVOAux.setInternalParameters(internalParameters);
      integrationVOAux.setName(integration.getName());
      integrationVOAux.setId(integration.getId());
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
      integrationCriteria.getInternalParameters().put(IntegrationParams.DATASET_SCHEMA_ID,
          originDatasetSchemaId);
      List<IntegrationVO> integrations = getAllIntegrationsByCriteria(integrationCriteria);
      Boolean existsExportEuDataset = integrations.stream()
          .anyMatch(integration -> IntegrationOperationTypeEnum.EXPORT_EU_DATASET
              .equals(integration.getOperation()));
      for (IntegrationVO integration : integrations) {
        // we've got the origin integrations. We intend to change the dataflow and the
        // datasetSchemaId
        // parameters and save all as new integrations of the copied design datasets
        LOG.info(
            "There are integrations to be copied into the datasetSchemaId {} in the dataflowId {}",
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId), dataflowIdDestination);
        integration.getInternalParameters().put(IntegrationParams.DATASET_SCHEMA_ID,
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId));
        integration.getInternalParameters().put("dataflowId", dataflowIdDestination.toString());
        integration.setId(null);
        CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
        crudManager.create(integration);
      }
      // create the automatic export eu dataset integration in case not existing
      if (Boolean.FALSE.equals(existsExportEuDataset)) {
        LOG.info(
            "There are no export eu dataset integration in the datasetSchemaId {}, dataflowId {}. Proceed to create it",
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId), dataflowIdDestination);
        createDefaultIntegration(dataflowIdDestination,
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId));
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
  @Transactional
  public List<ExecutionResultVO> executeEUDatasetExport(Long dataflowId) throws EEAException {

    // Get IntegrationVOs and EUDatasetVOs
    Map<Long, IntegrationVO> map = new HashMap<>();
    List<ExecutionResultVO> resultList = new ArrayList<>();
    List<EUDatasetVO> euDatasetVOs = euDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId);
    List<Integration> integrations = integrationRepository.findByOperationAndParameterAndValue(
        IntegrationOperationTypeEnum.EXPORT_EU_DATASET, IntegrationParams.DATAFLOW_ID,
        dataflowId.toString());
    List<IntegrationVO> integrationVOs = integrationMapper.entityListToClass(integrations);

    // Match each IntegrationVO with its EUDatasetVO
    for (IntegrationVO integrationVO : integrationVOs) {
      String datasetSchemaId =
          integrationVO.getInternalParameters().get(IntegrationParams.DATASET_SCHEMA_ID);
      for (EUDatasetVO euDatasetVO : euDatasetVOs) {
        if (datasetSchemaId.equals(euDatasetVO.getDatasetSchema())) {
          map.put(euDatasetVO.getId(), integrationVO);
          break;
        }
      }
    }

    // Execute integrations
    if (euDatasetVOs.size() == integrationVOs.size() && euDatasetVOs.size() == map.size()) {
      IntegrationExecutorService executor =
          integrationExecutorFactory.getExecutor(IntegrationToolTypeEnum.FME);
      for (Map.Entry<Long, IntegrationVO> entry : map.entrySet()) {
        resultList.add(executor.execute(IntegrationOperationTypeEnum.EXPORT_EU_DATASET, null,
            entry.getKey(), entry.getValue()));
      }
      return resultList;
    } else {
      LOG_ERROR.error(
          "Mismatching number of IntegrationVOs and EUDatasetVOs: integrationVOs={}, euDatasetVOs={}, map={}",
          integrationVOs, euDatasetVOs, map);
      throw new EEAException("Mismatching number of IntegrationVOs and EUDatasetVOs");
    }
  }

  /**
   * Creates the default integration.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaId the dataset schema id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void createDefaultIntegration(Long dataflowId, String datasetSchemaId)
      throws EEAException {
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.DATAFLOW_ID, dataflowId.toString());
    internalParameters.put(IntegrationParams.DATASET_SCHEMA_ID, datasetSchemaId);
    internalParameters.put(IntegrationParams.REPOSITORY, "ReportNetTesting");
    internalParameters.put(IntegrationParams.PROCESS_NAME, "Export_EU_dataset.fmw");

    Map<String, String> externalParameters = new HashMap<>();
    externalParameters.put(IntegrationParams.DATABASE_CONNECTION_PUBLIC, "");
    externalParameters.put(IntegrationParams.SCHEMA, "dbo");

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setDescription("Export EU Dataset");
    integrationVO.setInternalParameters(internalParameters);
    integrationVO.setExternalParameters(externalParameters);
    integrationVO.setName("Export EU Dataset");
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    integrationVO.setTool(IntegrationToolTypeEnum.FME);

    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.create(integrationVO);
  }

  /**
   * Delete export eu dataset.
   *
   * @param datasetSchemaId the dataset schema id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void deleteExportEuDataset(String datasetSchemaId) throws EEAException {
    IntegrationVO integration = getExportEUDatasetIntegration(datasetSchemaId);
    if (null != integration) {
      CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
      crudManager.delete(integration.getId());
    }
  }

  /**
   * Release populate EU dataset lock.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  public void releasePopulateEUDatasetLock(Long dataflowId) {
    Map<String, Object> populateEuDataset = new HashMap<>();
    populateEuDataset.put(LiteralConstants.SIGNATURE, LockSignature.POPULATE_EU_DATASET.getValue());
    populateEuDataset.put(LiteralConstants.DATAFLOWID, dataflowId);
    lockService.removeLockByCriteria(populateEuDataset);
  }

  /**
   * Adds the lock.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  @Override
  public void addPopulateEUDatasetLock(Long dataflowId) throws EEAException {
    // Lock to avoid export EUDataset while is copying data
    Map<String, Object> mapCriteriaExport = new HashMap<>();
    mapCriteriaExport.put("signature", LockSignature.POPULATE_EU_DATASET.getValue());
    mapCriteriaExport.put("dataflowId", dataflowId);
    lockService.createLock(new Timestamp(System.currentTimeMillis()),
        SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
        mapCriteriaExport);
  }

  /**
   * Gets the export EU dataset integration.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the export EU dataset integration
   */
  @Override
  public IntegrationVO getExportEUDatasetIntegration(String datasetSchemaId) {
    Integration integration = integrationRepository.findFirstByOperationAndParameterAndValue(
        IntegrationOperationTypeEnum.EXPORT_EU_DATASET, IntegrationParams.DATASET_SCHEMA_ID,
        datasetSchemaId);
    IntegrationVO integrationVO = integrationMapper.entityToClass(integration);
    LOG.debug("Found EXPORT_EU_DATASET integration: {}", integrationVO);
    return integrationVO;
  }

  /**
   * Gets the export integration.
   *
   * @param datasetSchemaId the dataset schema id
   * @param integrationId the integration id
   * @return the export integration
   */
  @Override
  public IntegrationVO getExportIntegration(String datasetSchemaId, Long integrationId) {
    List<Integration> integrations = integrationRepository.findByOperationAndParameterAndValue(
        IntegrationOperationTypeEnum.EXPORT, IntegrationParams.DATASET_SCHEMA_ID, datasetSchemaId);

    IntegrationVO integrationVO = null;
    if (null != integrations) {
      mainloop: for (Integration integration : integrations) {
        if (integration.getId().equals(integrationId)) {
          integrationVO = integrationMapper.entityToClass(integration);
          break mainloop;
        }
      }
    }

    if (null == integrationVO) {
      LOG_ERROR.error("No EXPORT integration: datasetSchemaId={}, integrationId={}",
          datasetSchemaId, integrationId);
    }
    return integrationVO;
  }

  /**
   * Delete schema integrations.
   *
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  @Transactional
  public void deleteSchemaIntegrations(String datasetSchemaId) {
    integrationRepository.deleteByParameterAndValue(IntegrationParams.DATASET_SCHEMA_ID,
        datasetSchemaId);
  }



  /**
   * Execute external integration.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   * @param operation the operation
   * @param replace the replace
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void executeExternalIntegration(Long datasetId, Long integrationId,
      IntegrationOperationTypeEnum operation, Boolean replace) throws EEAException {

    // Delete the previous data in the dataset if we have chosen it before the call to FME
    if (Boolean.TRUE.equals(replace)) {
      LOG.info("Replacing the data previous the execution of an external integration in dataset {}",
          datasetId);
      datasetControllerZuul.deleteDataBeforeReplacing(datasetId, integrationId, operation);
    } else {
      IntegrationVO integration = getIntegration(integrationId);
      if (null != integration && (Integer) integrationExecutorFactory
          .getExecutor(IntegrationToolTypeEnum.FME).execute(operation, null, datasetId, integration)
          .getExecutionResultParams().get("id") == 0) {
        LOG_ERROR.error("Error executing external integration: datasetId={}, integration={}",
            datasetId, integration);
        throw new EEAException("Error executing external integration");
      }
    }
  }


  /**
   * Creates the integrations.
   *
   * @param integrationsVO the integrations VO
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void createIntegrations(List<IntegrationVO> integrationsVO) throws EEAException {

    for (IntegrationVO integrationVO : integrationsVO) {
      CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
      crudManager.create(integrationVO);
    }
  }

  /**
   * Release locks.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void releaseLocks(Long datasetId) {

    Map<String, Object> insertRecords = new HashMap<>();
    insertRecords.put(LiteralConstants.SIGNATURE, LockSignature.INSERT_RECORDS.getValue());
    insertRecords.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> deleteRecords = new HashMap<>();
    deleteRecords.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_RECORDS.getValue());
    deleteRecords.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> updateField = new HashMap<>();
    updateField.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_FIELD.getValue());
    updateField.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> updateRecords = new HashMap<>();
    updateRecords.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_RECORDS.getValue());
    updateRecords.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> deleteDatasetValues = new HashMap<>();
    deleteDatasetValues.put(LiteralConstants.SIGNATURE,
        LockSignature.DELETE_DATASET_VALUES.getValue());
    deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> importFileData = new HashMap<>();
    importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
    importFileData.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> insertRecordsMultitable = new HashMap<>();
    insertRecordsMultitable.put(LiteralConstants.SIGNATURE,
        LockSignature.INSERT_RECORDS_MULTITABLE.getValue());
    insertRecordsMultitable.put(LiteralConstants.DATASETID, datasetId);

    lockService.removeLockByCriteria(insertRecords);
    lockService.removeLockByCriteria(deleteRecords);
    lockService.removeLockByCriteria(updateField);
    lockService.removeLockByCriteria(updateRecords);
    lockService.removeLockByCriteria(deleteDatasetValues);
    lockService.removeLockByCriteria(importFileData);
    lockService.removeLockByCriteria(insertRecordsMultitable);
  }



  /**
   * Adds the locks.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public void addLocks(Long datasetId) throws EEAException {
    Map<String, Object> insertRecords = new HashMap<>();
    insertRecords.put(LiteralConstants.SIGNATURE, LockSignature.INSERT_RECORDS.getValue());
    insertRecords.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> deleteRecords = new HashMap<>();
    deleteRecords.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_RECORDS.getValue());
    deleteRecords.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> updateField = new HashMap<>();
    updateField.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_FIELD.getValue());
    updateField.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> updateRecords = new HashMap<>();
    updateRecords.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_RECORDS.getValue());
    updateRecords.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> deleteDatasetValues = new HashMap<>();
    deleteDatasetValues.put(LiteralConstants.SIGNATURE,
        LockSignature.DELETE_DATASET_VALUES.getValue());
    deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);

    Map<String, Object> insertRecordsMultitable = new HashMap<>();
    insertRecordsMultitable.put(LiteralConstants.SIGNATURE,
        LockSignature.INSERT_RECORDS_MULTITABLE.getValue());
    insertRecordsMultitable.put(LiteralConstants.DATASETID, datasetId);

    createLockWithSignature(insertRecords);
    createLockWithSignature(deleteRecords);
    createLockWithSignature(updateField);
    createLockWithSignature(updateRecords);
    createLockWithSignature(deleteDatasetValues);
    createLockWithSignature(insertRecordsMultitable);
  }



  /**
   * Gets the integration.
   *
   * @param integrationId the integration id
   * @return the integration
   */
  @Override
  public IntegrationVO getIntegration(Long integrationId) {

    IntegrationVO integrationVO = null;
    Integration integration = integrationRepository.findById(integrationId).orElse(null);
    if (null != integration) {
      integrationVO = integrationMapper.entityToClass(integration);
    }

    if (null == integrationVO) {
      LOG_ERROR.error("No integration: integrationId={}", integrationId);
    }
    return integrationVO;
  }


  /**
   * Creates the lock with signature.
   *
   * @param lockSignature the lock signature
   * @param mapCriteria the map criteria
   * @param userName the user name
   * @throws EEAException the EEA exception
   */
  private void createLockWithSignature(Map<String, Object> lockCriteria) throws EEAException {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    lockService.createLock(new Timestamp(System.currentTimeMillis()), user, LockType.METHOD,
        lockCriteria);
  }
}
