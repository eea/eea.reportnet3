package org.eea.validation.kafka.command;

import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public abstract class CheckValidatedCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The validation helper.
   */
  @Autowired
  private ValidationHelper validationHelper;


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   *
   * @throws EEAException
   */
  @Override
  @Async
  public void execute(final EEAEventVO eeaEventVO) throws EEAException {
    final String uuid = (String) eeaEventVO.getData().get("uuid");
    final Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
    ConcurrentHashMap<String, Integer> processMap = validationHelper.getProcessesMap();
    synchronized (processMap) {
      if (processMap.containsKey(uuid)) {
        processMap.merge(uuid, -1, Integer::sum);
        validationHelper.checkFinishedValidations(datasetId, uuid);
      }
    }
  }

}
