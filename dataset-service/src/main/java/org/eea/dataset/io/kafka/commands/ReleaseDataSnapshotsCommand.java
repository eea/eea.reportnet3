package org.eea.dataset.io.kafka.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.io.FileUtils;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
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
  private DesignDatasetService designDatasetService;

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;
  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReleaseDataSnapshotsCommand.class);


  /** The Constant FILE_PUBLIC_DATASET_PATTERN_NAME. */
  private static final String FILE_PUBLIC_DATASET_PATTERN_NAME = "%s-%s.xlsx";

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

      try {
        createAllFiles(dataflowVO, dataset);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

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


  /**
   * Creates the all files.
   *
   * @param dataflowVO the dataflow VO
   * @param dataset the dataset
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createAllFiles(DataFlowVO dataflowVO, DataSetMetabase dataset) throws IOException {

    if (dataflowVO.isShowPublicInfo()) {
      List<RepresentativeVO> representativeList =
          representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowVO.getId());

      RepresentativeVO representative = representativeList.stream()
          .filter(data -> data.getDataProviderId() == dataset.getDataProviderId()).findAny()
          .orElse(null);

      if (null != representative && !representative.getReceiptDownloaded()) {

        DataProviderVO dataProvider =
            representativeControllerZuul.findDataProviderById(representative.getDataProviderId());

        List<DataSetMetabase> datasetMetabaseList =
            dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(dataflowVO.getId(),
                representative.getDataProviderId());


        List<DesignDatasetVO> desingDataset =
            designDatasetService.getDesignDataSetIdByDataflowId(dataflowVO.getId());

        // we find the name of the dataset to asing it for the notiFicaion
        String datasetDesingName = "";
        for (DesignDatasetVO designDatasetVO : desingDataset) {
          if (designDatasetVO.getDatasetSchema().equalsIgnoreCase(dataset.getDatasetSchema())) {
            datasetDesingName = designDatasetVO.getDataSetName();
          }
        }

        Path path = Paths.get("C:\\importFilesPublic\\dataflow-" + dataflowVO.getId());
        File directoryDataflow = new File(path.toString());
        if (!directoryDataflow.exists()) {
          Files.createDirectories(path);
        }
        Path pathDataProvider = Paths.get("C:\\importFilesPublic\\dataflow-" + dataflowVO.getId()
            + "\\dataProvider-" + dataProvider.getLabel());
        if (directoryDataflow.exists()) {
          FileUtils.deleteDirectory(new File(pathDataProvider.toString()));
        }
        Files.createDirectories(pathDataProvider);


        for (DataSetMetabase datasetToFile : datasetMetabaseList) {
          if (!datasetToFile.isAvailableInPublic()) {

            try {
              byte[] file = datasetService.exportFile(datasetToFile.getId(), "xlsx", null);
              String nameFileUnique = String.format(FILE_PUBLIC_DATASET_PATTERN_NAME,
                  dataProvider.getLabel(), datasetDesingName);

              String newFile = pathDataProvider.toString() + "\\" + nameFileUnique;

              FileUtils.writeByteArrayToFile(new File(newFile), file);
              dataset.setPublicFileName(nameFileUnique);
              dataSetMetabaseRepository.save(dataset);

            } catch (EEAException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }

          }
        }
      }

    }
  }


  /**
   * Cleanup directory.
   *
   * @param dir the dir
   */
  private void cleanupDirectory(File dir) {
    for (File file : dir.listFiles()) {
      if (file.isDirectory())
        cleanupDirectory(file);
      file.delete();
    }
  }
}
