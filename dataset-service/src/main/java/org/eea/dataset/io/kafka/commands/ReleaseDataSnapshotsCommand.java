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
import org.springframework.beans.factory.annotation.Value;
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

  /** The design dataset service. */
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

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR =
      LoggerFactory.getLogger(ReleaseDataSnapshotsCommand.class);

  /** The Constant FILE_PUBLIC_DATASET_PATTERN_NAME. */
  private static final String FILE_PUBLIC_DATASET_PATTERN_NAME = "%s-%s.xlsx";


  /** The path public file. */
  @Value("${pathPublicFile}")
  private String pathPublicFile;


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
        LOG_ERROR.error("Folder not created in dataflow {} with dataprovider {} message {}",
            dataset.getDataflowId(), dataset.getDataProviderId(), e.getMessage(), e);
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

    // 1 we ceck if the dataflow is avaliable to publish
    if (dataflowVO.isShowPublicInfo()) {

      LOG.info("Start creating files. DataflowId: {} DataProviderId: {}", dataset.getDataflowId(),
          dataset.getDataProviderId());


      List<RepresentativeVO> representativeList =
          representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowVO.getId());

      // we took representative
      RepresentativeVO representative = representativeList.stream()
          .filter(data -> data.getDataProviderId() == dataset.getDataProviderId()).findAny()
          .orElse(null);

      // we check if the representative have permit to do it
      if (null != representative && !representative.getReceiptDownloaded()) {

        DataProviderVO dataProvider =
            representativeControllerZuul.findDataProviderById(representative.getDataProviderId());

        List<DataSetMetabase> datasetMetabaseList =
            dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(dataflowVO.getId(),
                representative.getDataProviderId());



        // we create the dataflow folder to save it
        Path pathDataflow = Paths.get(new StringBuilder(pathPublicFile).append("dataflow-")
            .append(dataflowVO.getId()).toString());
        File directoryDataflow = new File(pathDataflow.toString());
        if (!directoryDataflow.exists()) {
          Files.createDirectories(pathDataflow);

          LOG.info("Folder {} created", pathDataflow);
        }

        // we create the dataprovider folder to save it andwe always delete it and put new files
        Path pathDataProvider = Paths
            .get(new StringBuilder(pathPublicFile).append("dataflow-").append(dataflowVO.getId())
                .append("\\dataProvider-").append(dataProvider.getLabel()).toString());
        if (directoryDataflow.exists()) {
          FileUtils.deleteDirectory(new File(pathDataProvider.toString()));
        }
        Files.createDirectories(pathDataProvider);
        LOG.info("Folder {} created", pathDataProvider);

        creeateAllDatasetFiles(dataset, dataflowVO.getId(), dataProvider, datasetMetabaseList,
            pathDataProvider);
      }

    }
  }


  /**
   * Creeate all dataset files.
   *
   * @param dataset the dataset
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   * @param datasetMetabaseList the dataset metabase list
   * @param pathDataProvider the path data provider
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void creeateAllDatasetFiles(DataSetMetabase dataset, Long dataflowId,
      DataProviderVO dataProvider, List<DataSetMetabase> datasetMetabaseList, Path pathDataProvider)
      throws IOException {
    // now we create all files depends if they are avaliable
    for (DataSetMetabase datasetToFile : datasetMetabaseList) {
      if (!datasetToFile.isAvailableInPublic()) {

        // we put the good in the correct field
        List<DesignDatasetVO> desingDataset =
            designDatasetService.getDesignDataSetIdByDataflowId(dataflowId);

        // we find the name of the dataset to asing it for file
        String datasetDesingName = "";
        for (DesignDatasetVO designDatasetVO : desingDataset) {
          if (designDatasetVO.getDatasetSchema()
              .equalsIgnoreCase(datasetToFile.getDatasetSchema())) {
            datasetDesingName = designDatasetVO.getDataSetName();
          }
        }

        try {
          // 1º we create
          byte[] file = datasetService.exportFile(datasetToFile.getId(), "xlsx", null);
          String nameFileUnique = String.format(FILE_PUBLIC_DATASET_PATTERN_NAME,
              dataProvider.getLabel(), datasetDesingName);


          String newFile = new StringBuilder(pathDataProvider.toString()).append("\\")
              .append(nameFileUnique).toString();

          FileUtils.writeByteArrayToFile(new File(newFile), file);
          datasetToFile.setPublicFileName(newFile);
          dataSetMetabaseRepository.save(datasetToFile);

        } catch (EEAException e) {
          LOG_ERROR.error(
              "File not created in dataflow {} with dataprovider {} with datasetId {} message {}",
              dataset.getDataflowId(), dataset.getDataProviderId(), datasetToFile.getId(),
              e.getMessage(), e);
        }
        LOG.info("Start files created in DataflowId: {} with DataProviderId: {}",
            dataset.getDataflowId(), dataset.getDataProviderId());
      }
    }
  }
}
