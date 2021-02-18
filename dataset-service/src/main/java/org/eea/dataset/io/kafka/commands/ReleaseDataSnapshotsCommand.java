package org.eea.dataset.io.kafka.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class ReleaseDataSnapshotsCommand extends AbstractEEAEventHandlerCommand {


  /** The dataset metabase controller. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;
  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReleaseDataSnapshotsCommand.class);


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.RELEASE_ONEBYONE_COMPLETED_EVENT;
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

    Long nextData = datasetMetabaseService.getLastDatasetValidationForRelease(datasetId);
    if (null != nextData) {
      CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
      createSnapshotVO.setReleased(true);
      createSnapshotVO.setAutomatic(Boolean.TRUE);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      Date ahora = new Date();
      SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      createSnapshotVO.setDescription("Release " + formateador.format(ahora));
      datasetSnapshotService.addSnapshot(nextData, createSnapshotVO, null);
    } else {
      DataSetMetabase dataset =
          dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());
      // now when all finish we create the file to save the data to public export

      DataFlowVO dataflowVO = dataflowControllerZuul.findById(dataset.getDataflowId());



      // try {

      // List<DesignDatasetVO> desingDataset =
      // designDatasetService.getDesignDataSetIdByDataflowId(dataflow.getId());
      // // we find the name of the dataset to asing it for the notiFicaion
      // String datasetName = "";
      // for (DesignDatasetVO designDatasetVO : desingDataset) {
      // if (designDatasetVO.getDatasetSchema()
      // .equalsIgnoreCase(designDataset.getDatasetSchema())) {
      // datasetName = designDatasetVO.getDataSetName();
      // }
      // }
      //
      //
      // byte[] file = datasetService.exportFile(idDataset, "xlsx", null);
      // String nameFileUnique =
      // String.format(FILE_PUBLIC_DATASET_PATTERN_NAME, provider.getLabel(), datasetName);
      // Path path = Paths.get("C:\\importFilesPublic\\dataflow-" + dataflow.getId());
      // File directory = new File(path.toString());
      // if (!directory.exists()) {
      // Files.createDirectories(path);
      // }
      // String newFile = path.toString() + "\\" + nameFileUnique;
      // // FileWriter fileWriter = new FileWriter(newFile, false);
      // // BufferedWriter bw = new BufferedWriter(fileWriter);
      // // bw.write(file);
      // // bw.close();
      // // Files.write(new File(newFile), file);
      // FileUtils.writeByteArrayToFile(new File(newFile), file);
      // designDataset.setPublicFileName(nameFileUnique);
      // metabaseRepository.save(designDataset);
      //
      // } catch (IOException e) {
      // LOG.info("Error creating public file : dataflowId={}, datasetId={}, providerId={}",
      // dataflow.getId(), idDataset, provider.getId());
      // }



      // At this point the process of releasing all the datasets has been finished so we unlock
      // everything involved
      datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(),
          dataset.getDataProviderId());

      LOG.info("Releasing datasets process ends. DataflowId: {} DataProviderId: {}",
          dataset.getDataflowId(), dataset.getDataProviderId());
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataset.getDataflowId()).dataflowName(dataflowVO.getName())
              .providerId(dataset.getDataProviderId()).build());
    }

  }


  private void createAllFiles(DataFlowVO dataflowVO, DataSetMetabase dataset) {

    List<RepresentativeVO> representativeList =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowVO.getId());

    RepresentativeVO representative = representativeList.stream()
        .filter(data -> data.getId() == dataset.getDataProviderId()).findAny().orElse(null);

    if (dataflowVO.isShowPublicInfo()) {

    }
  }

}
