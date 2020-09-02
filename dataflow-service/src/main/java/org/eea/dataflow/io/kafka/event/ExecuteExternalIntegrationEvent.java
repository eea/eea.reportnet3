package org.eea.dataflow.io.kafka.event;

import java.util.List;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;



/**
 * The Class ExecuteExternalIntegrationEvent.
 */
@Component
public class ExecuteExternalIntegrationEvent extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The integration service. */
  @Autowired
  private IntegrationService integrationService;

  /** The integration executor factory. */
  @Autowired
  private IntegrationExecutorFactory integrationExecutorFactory;



  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.DATA_DELETE_TO_REPLACE_COMPLETED_EVENT;
  }


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {

    Long datasetId =
        Long.parseLong(eeaEventVO.getData().get(LiteralConstants.DATASET_ID).toString());
    Long integrationId =
        Long.parseLong(eeaEventVO.getData().get(LiteralConstants.INTEGRATION_ID).toString());
    IntegrationOperationTypeEnum operation = IntegrationOperationTypeEnum
        .valueOf(eeaEventVO.getData().get(LiteralConstants.OPERATION).toString());

    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationVO.setId(integrationId);
      List<IntegrationVO> integrations =
          integrationService.getAllIntegrationsByCriteria(integrationVO);

      if (integrations != null && !integrations.isEmpty()) {
        integrationExecutorFactory.getExecutor(IntegrationToolTypeEnum.FME).execute(operation, null,
            datasetId, integrations.get(0));
      }
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error executing an external integration with id {} on the datasetId {}, with message: {}",
          integrationId, datasetId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


}
