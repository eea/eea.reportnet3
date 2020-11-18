package org.eea.dataflow.integration.crud.factory.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.dataflow.integration.crud.factory.AbstractCrudManager;
import org.eea.dataflow.integration.utils.IntegrationParams;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.dataflow.persistence.repository.OperationParametersRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class FMEIntegrationManager.
 */
@Component
public class FMEIntegrationManager extends AbstractCrudManager {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationManager.class);

  /** The integration repository. */
  @Autowired
  private IntegrationRepository integrationRepository;

  /** The integration mapper. */
  @Autowired
  private IntegrationMapper integrationMapper;

  /** The operation parameters repository. */
  @Autowired
  private OperationParametersRepository operationParametersRepository;

  /**
   * Gets the tool type.
   *
   * @return the tool type
   */
  @Override
  public IntegrationToolTypeEnum getToolType() {
    return IntegrationToolTypeEnum.FME;
  }

  /**
   * Gets the.
   *
   * @param integrationVO the integration VO
   * @return the list
   * @throws EEAException the EEA exception
   */
  @Override
  public List<IntegrationVO> get(IntegrationVO integrationVO) throws EEAException {

    List<IntegrationVO> results = new ArrayList<>();
    if (integrationVO.getId() != null) {
      Optional<Integration> integrationOptional =
          integrationRepository.findById(integrationVO.getId());
      Integration integration = integrationOptional.isPresent() ? integrationOptional.get() : null;
      results.add(integrationMapper.entityToClass(integration));
    } else if (integrationVO.getInternalParameters() != null
        && integrationVO.getInternalParameters().size() > 0) {
      if (integrationVO.getInternalParameters().containsKey(IntegrationParams.DATAFLOW_ID)) {
        integrationVO.getInternalParameters().remove(IntegrationParams.DATAFLOW_ID);
      }
      List<String> parameters = new ArrayList<>(integrationVO.getInternalParameters().keySet());
      String parameter = parameters.get(0);
      String value = integrationVO.getInternalParameters().get(parameter);
      List<Integration> integrationList =
          integrationRepository.findByInternalOperationParameter(parameter, value);
      results = integrationMapper.entityListToClass(integrationList);
    } else {
      LOG_ERROR.error("No filters in the query to find integrations");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }

    return results;
  }

  /**
   * Checkname.
   *
   * @param integration the integration
   * @param integrationVO the integration VO
   */
  private void checkname(Integration integration, IntegrationVO integrationVO) {

    List<Integration> existingIntegrations =
        integrationRepository.findByInternalOperationParameter(IntegrationParams.DATAFLOW_ID,
            integrationVO.getInternalParameters().get(IntegrationParams.DATAFLOW_ID));
    String integrationSchemaId =
        integrationVO.getInternalParameters().get(IntegrationParams.DATASET_SCHEMA_ID);
    for (Integration integrationAux : existingIntegrations) {
      List<InternalOperationParameters> internalParameterAux =
          integrationAux.getInternalParameters();
      for (InternalOperationParameters internalParameter : internalParameterAux) {
        if (internalParameter.getParameter().equals(IntegrationParams.DATASET_SCHEMA_ID)
            && internalParameter.getValue().equals(integrationSchemaId)) {
          if (integrationAux.getName().equals(integration.getName())) {
            LOG_ERROR.error("Error creating an integration: Integration name is duplicated ");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                EEAErrorMessage.DUPLICATED_INTEGRATION_NAME);
          }
        }
      }
    }

  }

  /**
   * Update.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void update(IntegrationVO integrationVO) throws EEAException {

    Integration integration = integrationRepository.findById(integrationVO.getId()).orElse(null);
    if (integration == null) {
      throw new EEAException(EEAErrorMessage.INTEGRATION_NOT_FOUND);
    }

    // Update the default integration EXPORT_EU_DATASET
    if (IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(integration.getOperation())
        || IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(integrationVO.getOperation())) {
      integrationVO = updateDefaultIntegration(integration, integrationVO);
    }

    if (integrationVO.getInternalParameters() == null
        || integrationVO.getInternalParameters().size() == 0
        || !integrationVO.getInternalParameters().containsKey(IntegrationParams.DATAFLOW_ID)
        || !integrationVO.getInternalParameters()
            .containsKey(IntegrationParams.DATASET_SCHEMA_ID)) {
      LOG_ERROR.error(
          "Error updating an integration: Internal parameters don't have dataflowId or datasetSchemaId");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }

    checkname(integration, integrationVO);

    operationParametersRepository.deleteByIntegration(integration);
    integration = integrationMapper.classToEntity(integrationVO);

    integrationRepository.save(integration);
    LOG.info("Integration updated: {}", integrationVO);
  }

  /**
   * Creates the.
   *
   * @param integrationVO the integration VO
   */
  @Override
  public void create(IntegrationVO integrationVO) {

    if (integrationVO.getInternalParameters() == null
        || integrationVO.getInternalParameters().size() == 0
        || !integrationVO.getInternalParameters().containsKey(IntegrationParams.DATAFLOW_ID)
        || !integrationVO.getInternalParameters()
            .containsKey(IntegrationParams.DATASET_SCHEMA_ID)) {
      LOG_ERROR.error(
          "Error creating an integration: Internal parameters don't have dataflowId or datasetSchemaId");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }

    Integration integration = integrationMapper.classToEntity(integrationVO);

    checkname(integration, integrationVO);

    integrationRepository.save(integration);
    LOG.info("Integration created: {}", integrationVO);
  }

  /**
   * Delete.
   *
   * @param integrationId the integration id
   * @throws EEAException the EEA exception
   */
  @Override
  public void delete(Long integrationId) throws EEAException {
    if (integrationId != null) {
      integrationRepository.deleteById(integrationId);
      LOG.info("Integration with Id {} deleted", integrationId);
    } else {
      LOG_ERROR.error("IntegrationId missing");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }
  }

  /**
   * Update default integration.
   *
   * @param integration the integration
   * @param integrationVO the integration VO
   * @return the integration VO
   */
  private IntegrationVO updateDefaultIntegration(Integration integration,
      IntegrationVO integrationVO) {

    if (!integration.getOperation().equals(integrationVO.getOperation())) {
      LOG_ERROR.error(
          "Error updating an integration: Cannot modify the operation type from/to EXPORT_EU_DATASET. integration = {}, integrationVO = {}",
          integration, integrationVO);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.OPERATION_TYPE_NOT_EDITABLE);
    }

    Map<String, String> newInternalParameters = integrationVO.getInternalParameters();
    Map<String, String> newExternalParameters = integrationVO.getExternalParameters();
    String name = integrationVO.getName();
    String description = integrationVO.getDescription();

    integrationVO = integrationMapper.entityToClass(integration);

    updateInternalParameters(integrationVO, newInternalParameters);

    if (null != newExternalParameters) {
      integrationVO.setExternalParameters(newExternalParameters);
    }

    if (null != name && !name.isEmpty()) {
      integrationVO.setName(name);
    }

    if (null != description && !description.isEmpty()) {
      integrationVO.setDescription(description);
    }

    return integrationVO;
  }

  /**
   * Update internal parameters.
   *
   * @param integrationVO the integration VO
   * @param newInternalParameters the new internal parameters
   */
  private void updateInternalParameters(IntegrationVO integrationVO,
      Map<String, String> newInternalParameters) {

    Map<String, String> oldInternalParametersMap = integrationVO.getInternalParameters();
    String dataflowId = newInternalParameters.get(IntegrationParams.DATAFLOW_ID);
    String datasetSchemaId = newInternalParameters.get(IntegrationParams.DATASET_SCHEMA_ID);
    String repository = newInternalParameters.get(IntegrationParams.REPOSITORY);
    String processName = newInternalParameters.get(IntegrationParams.PROCESS_NAME);
    String databaseConnection =
        newInternalParameters.get(IntegrationParams.DATABASE_CONNECTION_PUBLIC);

    if (null != dataflowId && !dataflowId.isEmpty()) {
      oldInternalParametersMap.put(IntegrationParams.DATAFLOW_ID, dataflowId);
    }

    if (null != datasetSchemaId && !datasetSchemaId.isEmpty()) {
      oldInternalParametersMap.put(IntegrationParams.DATASET_SCHEMA_ID, datasetSchemaId);
    }

    if (null != repository && !repository.isEmpty()) {
      oldInternalParametersMap.put(IntegrationParams.REPOSITORY, repository);
    }

    if (null != processName && !processName.isEmpty()) {
      oldInternalParametersMap.put(IntegrationParams.PROCESS_NAME, processName);
    }

    if (null != databaseConnection) {
      oldInternalParametersMap.put(IntegrationParams.DATABASE_CONNECTION_PUBLIC,
          databaseConnection);
    }
  }
}
