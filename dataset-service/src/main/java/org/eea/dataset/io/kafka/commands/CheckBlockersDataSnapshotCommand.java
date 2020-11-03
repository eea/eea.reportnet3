package org.eea.dataset.io.kafka.commands;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class CheckBlockersDataSnapshotCommand extends AbstractEEAEventHandlerCommand {


  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The validation repository. */
  @Autowired
  private ValidationRepository validationRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExecutePropagateNewFieldCommand.class);

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
    return EventType.VALIDATION_RELEASE_FINISHED_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));

    // with one id we take all the datasets with the same dataProviderId and dataflowId
    DataSetMetabase dataset = dataSetMetabaseRepository.findById(datasetId).get();
    List<Long> datasets = dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(
        dataset.getDataflowId(), dataset.getDataProviderId());
    Collections.sort(datasets);
    // we check if one or more dataset have error, if have we create a notification and abort
    // process of releasing
    boolean haveBlockers = false;
    for (Long id : datasets) {
      setTenant(id);
      if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
        haveBlockers = true;
        // Release the locks
        datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(),
            dataset.getDataProviderId());
        LOG_ERROR.error("Error releasing, the datasets have blockers errors");
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_BLOCKERS_FAILED_EVENT, null,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .datasetId(datasetId)
                .error("One or more datasets have blockers errors, Release aborted")
                .providerId(dataset.getDataProviderId()).build());
        break;
      }
    }
    // if we havent blockers we will do release 1st dataset and do it one by one
    if (!haveBlockers) {
      LOG.info("Release datasets in dataflow {} starts", dataset.getDataflowId());
      CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
      createSnapshotVO.setReleased(true);
      createSnapshotVO.setAutomatic(Boolean.TRUE);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      Date ahora = new Date();
      SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      createSnapshotVO.setDescription("Release " + formateador.format(ahora));
      datasetSnapshotService.addSnapshot(datasets.get(0), createSnapshotVO, null);
    }
  }

  /**
   * Sets the tenant.
   *
   * @param idDataset the new tenant
   */
  private void setTenant(Long idDataset) {
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
  }

}
