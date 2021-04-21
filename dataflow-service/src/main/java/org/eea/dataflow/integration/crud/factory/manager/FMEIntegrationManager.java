package org.eea.dataflow.integration.crud.factory.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataflow.integration.crud.factory.AbstractCrudManager;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.dataflow.persistence.repository.OperationParametersRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
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
   * @throws EEAException
   */
  private void checkName(Integration integration, IntegrationVO integrationVO) throws EEAException {
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
          if (!IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(integrationAux.getOperation())
              && integrationAux.getName().trim().equalsIgnoreCase(integration.getName().trim())) {
            LOG_ERROR.error(
                "Error creating an integration: Integration {} with name {} is duplicated in Dataflow: {}",
                integration.getId(), integration.getName(), integration.getDataflow());
            throw new EEAException(
                String.format("Integration %s with name %s is duplicated in Dataflow: %s",
                    integration.getId(), integration.getName(), integration.getDataflow()));
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
    if ((IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(integration.getOperation())
        || IntegrationOperationTypeEnum.EXPORT_EU_DATASET.equals(integrationVO.getOperation()))
        && !integration.getOperation().equals(integrationVO.getOperation())) {

      LOG_ERROR.error(
          "Error updating an integration: Cannot modify the operation type from/to EXPORT_EU_DATASET. integration = {}, integrationVO = {}",
          integration, integrationVO);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.OPERATION_TYPE_NOT_EDITABLE);
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

    operationParametersRepository.deleteByIntegration(integration);
    integration = integrationMapper.classToEntity(integrationVO);
    checkName(integration, integrationVO);
    integrationRepository.save(integration);
    LOG.info("Integration updated: {}", integrationVO);
  }

  /**
   * Creates the.
   *
   * @param integrationVO the integration VO
   */
  @Override
  public void create(IntegrationVO integrationVO) throws EEAException {

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

    checkName(integration, integrationVO);

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


}
