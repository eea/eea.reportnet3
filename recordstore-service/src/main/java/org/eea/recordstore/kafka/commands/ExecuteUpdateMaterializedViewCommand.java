package org.eea.recordstore.kafka.commands;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The Class ExecuteUpdateMaterialicedViewCommand.
 */
@Component
public class ExecuteUpdateMaterializedViewCommand extends AbstractEEAEventHandlerCommand {

  /** The database management service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /** The process controller zuul. */
  @Autowired
  private ProcessControllerZuul processControllerZuul;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.UPDATE_MATERIALIZED_VIEW_EVENT;
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
        Long.parseLong(String.valueOf(eeaEventVO.getData().get(LiteralConstants.DATASET_ID)));
    String user = String.valueOf(eeaEventVO.getData().get(LiteralConstants.USER));
    Boolean released = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("released")));
    String processId = String.valueOf(eeaEventVO.getData().get("processId"));
    List<Integer> referencesToRefresh =
        (List<Integer>) eeaEventVO.getData().get("referencesToRefresh");

    if (referencesToRefresh != null && !CollectionUtils.isEmpty(referencesToRefresh)) {
      referencesToRefresh.stream().forEach(dataset -> {
        try {
          recordStoreService.launchUpdateMaterializedQueryView(Long.valueOf(dataset));
        } catch (RecordStoreAccessException e) {
          LOG_ERROR.error("Error refreshing the materialized view of the dataset {}", dataset, e);
          processControllerZuul.updateProcess(datasetId, -1L, ProcessStatusEnum.CANCELED,
              ProcessTypeEnum.VALIDATION, processId,
              SecurityContextHolder.getContext().getAuthentication().getName(), 0, null);
          datasetMetabaseControllerZuul.updateDatasetRunningStatus(datasetId,
              DatasetRunningStatusEnum.ERROR_IN_VALIDATION);
        }
      });
    }
    recordStoreService.updateMaterializedQueryView(datasetId, user, released, processId);
  }

}
