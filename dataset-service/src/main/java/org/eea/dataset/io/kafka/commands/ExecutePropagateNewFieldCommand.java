package org.eea.dataset.io.kafka.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;


/**
 * The Class ExecutePropagateNewFieldCommand.
 */
@Component
public class ExecutePropagateNewFieldCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExecutePropagateNewFieldCommand.class);


  /**
   * The dataset service.
   */
  @Autowired
  private DatasetService datasetService;

  /**
   * The update record helper.
   */
  @Autowired
  private UpdateRecordHelper updateRecordHelper;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * The field batch size.
   */
  @Value("${dataset.propagation.fieldBatchSize}")
  private int fieldBatchSize;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION;
  }


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    String idTableSchema = (String) eeaEventVO.getData().get("idTableSchema");
    Integer numPag = (Integer) eeaEventVO.getData().get("numPag");
    String fieldSchemaId = (String) eeaEventVO.getData().get("idFieldSchema");
    DataType typeField = DataType.fromValue(eeaEventVO.getData().get("typeField").toString());
    final String uuid = (String) eeaEventVO.getData().get("uuId");

    try {
      Pageable pageable = PageRequest.of(numPag, fieldBatchSize);
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      datasetService.saveNewFieldPropagation(datasetId, idTableSchema, pageable, fieldSchemaId,
          typeField);
      LOG.info("field {} propagated", fieldSchemaId);
    } catch (Exception e) {
      LOG_ERROR.error("Error processing propagations for new field column in dataset {}", datasetId,
          e);
      eeaEventVO.getData().put("error", e);
    } finally {
      // if this is the coordinator propagation instance, then no need to send message, just updates
      // expected propagations
      ConcurrentHashMap<String, Integer> processMap = updateRecordHelper.getProcessesMap();
      synchronized (processMap) {
        if (processMap.containsKey(uuid)) {
          processMap.merge(uuid, -1, Integer::sum);
          // Release the delete field schema lock
          if (processMap.get(uuid) == 0) {
            List<Object> criteria = new ArrayList<>();
            criteria.add(LockSignature.DELETE_FIELD_SCHEMA.getValue());
            criteria.add(datasetId);
            criteria.add(fieldSchemaId);
            lockService.removeLockByCriteria(criteria);
          }

        }
      }
    }


  }


}
