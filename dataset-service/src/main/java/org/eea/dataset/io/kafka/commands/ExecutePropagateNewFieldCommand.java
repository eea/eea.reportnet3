package org.eea.dataset.io.kafka.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eea.dataset.service.DatasetService;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
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

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ExecutePropagateNewFieldCommand.class);

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;


  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The field batch size. */
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
    String tableSchemaId = (String) eeaEventVO.getData().get("idTableSchema");
    Integer numPag = (Integer) eeaEventVO.getData().get("numPag");
    String fieldSchemaId = (String) eeaEventVO.getData().get("idFieldSchema");
    DataType typeField = DataType.fromValue(eeaEventVO.getData().get("typeField").toString());
    final String uuid = (String) eeaEventVO.getData().get("uuId");
    Set<Integer> pages = new HashSet<>((ArrayList<Integer>) eeaEventVO.getData().get("pages"));
    try {
      Pageable pageable = PageRequest.of(numPag, fieldBatchSize);
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      datasetService.saveNewFieldPropagation(datasetId, tableSchemaId, pageable, fieldSchemaId,
          typeField);
      LOG.info("field {} from datasetId {} propagated", fieldSchemaId, datasetId);
    } catch (Exception e) {
      LOG_ERROR.error("Error processing propagations for new field column in dataset {}", datasetId,
          e);
      eeaEventVO.getData().put("error", e);
      removeLockDeleteFieldSchema(datasetId, fieldSchemaId);
    } finally {
      pages.remove(numPag);
      numPag++;
      if (pages.isEmpty()) {
        removeLockDeleteFieldSchema(datasetId, fieldSchemaId);
      } else {
        Map<String, Object> value = new HashMap<>();
        value.put("dataset_id", datasetId);
        value.put("idTableSchema", tableSchemaId);
        value.put("pages", pages);
        value.put("idFieldSchema", fieldSchemaId);
        value.put("typeField", typeField);
        value.put("uuId", uuid);
        value.put("numPag", numPag);
        kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION,
            value);
      }
      // now the view is not updated, update the check to false
      datasetService.updateCheckView(datasetId, false);
    }
  }


  /**
   * Removes the lock delete field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  private void removeLockDeleteFieldSchema(Long datasetId, String fieldSchemaId) {
    Map<String, Object> deleteFieldSchema = new HashMap<>();
    deleteFieldSchema.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_FIELD_SCHEMA.getValue());
    deleteFieldSchema.put(LiteralConstants.DATASETID, datasetId);
    deleteFieldSchema.put(LiteralConstants.FIELDSCHEMAID, fieldSchemaId);
    lockService.removeLockByCriteria(deleteFieldSchema);
  }
}
