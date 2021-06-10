package org.eea.dataset.io.kafka.commands;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.lock.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class PropagateNewFieldCommand extends AbstractEEAEventHandlerCommand {



  /**
   * The update record helper.
   */
  @Autowired
  private UpdateRecordHelper updateRecordHelper;

  /** The lock service. */
  @Autowired
  private LockService lockService;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_NEW_DESIGN_FIELD_PROPAGATION;
  }


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    Integer sizeRecords = (Integer) eeaEventVO.getData().get("sizeRecords");
    String idTableSchema = (String) eeaEventVO.getData().get("idTableSchema");
    Integer numPag = (Integer) eeaEventVO.getData().get("numPag");
    String fieldSchemaId = (String) eeaEventVO.getData().get("idFieldSchema");
    DataType typeField = DataType.fromValue(eeaEventVO.getData().get("typeField").toString());

    // Add lock to the delete fieldschema operation, to avoid the simultaneous propagation/deleting
    // of a new field
    Map<String, Object> mapCriteria = new HashMap<>();
    mapCriteria.put("signature", LockSignature.DELETE_FIELD_SCHEMA.getValue());
    mapCriteria.put("datasetId", datasetId);
    mapCriteria.put("fieldSchemaId", fieldSchemaId);
    lockService.createLock(new Timestamp(System.currentTimeMillis()),
        SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
        mapCriteria);

    updateRecordHelper.propagateNewFieldDesign(datasetId, idTableSchema, sizeRecords, numPag,
        UUID.randomUUID().toString(), fieldSchemaId, typeField);

  }


}
