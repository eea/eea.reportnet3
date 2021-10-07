package org.eea.dataflow.service.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.AutomaticRuleTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataflowHelp.
 */
@Component
public class DataflowHelp implements DisposableBean {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowHelp.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The import executor service. */
  private ExecutorService importExecutorService;

  /** The max running tasks. */
  @Value("${dataflow.task.parallelism}") // esto es dataset, tendrái que ser dataflow supongo
  private int maxRunningTasks;

  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The dataset schema controller zuul. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  /** The rules controller zuul. */
  @Autowired
  RulesControllerZuul rulesControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The Constant DOWNLOAD_SCHEMA_INFORMATION_EXCEPTION: {@value}. */
  private static final String DOWNLOAD_SCHEMA_INFORMATION_EXCEPTION =
      "Download exported Schema Information didn't found a file with the followings parameters:, dataflowId: %s + filename: %s";

  /** The path export dataflow schema information. */
  @Value("${exportDataflowSchemaInformationPath}")
  private String pathExportDataflowSchemaInformation;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    importExecutorService = new EEADelegatingSecurityContextExecutorService(
        Executors.newFixedThreadPool(maxRunningTasks));
  }

  /**
   * Destroy.
   *
   * @throws Exception the exception
   */
  @Override
  public void destroy() throws Exception {
    if (null != importExecutorService) {
      this.importExecutorService.shutdown();
    }
  }

  /**
   * Export schema information.
   *
   * @param dataflowId the dataflow id
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Async
  public void exportSchemaInformation(Long dataflowId) throws IOException, EEAException {



    String composedFileName = "dataflow-" + dataflowId + "-Schema_Information_"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss"));
    String fileNameWithExtension = composedFileName + "." + FileTypeEnum.XLSX.getValue();
    File fileFolder = new File(pathExportDataflowSchemaInformation, composedFileName);
    fileFolder.mkdirs();

    String creatingFileError =
        String.format("Failed generating schema information file with name %s from dataflowId %s",
            fileNameWithExtension, dataflowId);

    NotificationVO notificationVO = NotificationVO.builder().dataflowId(dataflowId)
        .user(SecurityContextHolder.getContext().getAuthentication().getName())
        .fileName(fileNameWithExtension).error(creatingFileError).build();

    File fileWrite = new File(new File(pathExportDataflowSchemaInformation, composedFileName),
        fileNameWithExtension);
    OutputStream out = new FileOutputStream(fileWrite.toString());
    try {
      byte[] dataFile = writeFile(dataflowId);

      out.write(dataFile);

      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.EXPORT_SCHEMA_INFORMATION_COMPLETED_EVENT, null, notificationVO);

    } catch (IOException e) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.EXPORT_SCHEMA_INFORMATION_FAILED_EVENT, null, notificationVO);
      LOG_ERROR.error(
          "Error downloading file generated from export from the dataflowId {}. Filename {}. Message: {}",
          dataflowId, fileNameWithExtension, e.getMessage());
    } finally {
      out.close();
    }

  }

  /**
   * Download schema information.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ResponseStatusException the response status exception
   */
  public File downloadSchemaInformation(Long dataflowId, String fileName)
      throws IOException, ResponseStatusException {
    String folderName = fileName.replace(".xlsx", "");
    // we compound the route and create the file
    File file = new File(new File(pathExportDataflowSchemaInformation, folderName), fileName);
    if (!file.exists()) {
      LOG_ERROR.error(String.format(DOWNLOAD_SCHEMA_INFORMATION_EXCEPTION, dataflowId, fileName));
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format(DOWNLOAD_SCHEMA_INFORMATION_EXCEPTION, dataflowId, fileName));
    }
    return file;
  }

  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private byte[] writeFile(Long dataflowId) throws IOException {


    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
    List<DesignDatasetVO> listDesignDatasetVO =
        datasetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId);
    List<RulesSchemaVO> listRulesSchemaVO = new ArrayList<RulesSchemaVO>();
    List<DataSetMetabaseVO> listDataSetMetabaseVO = new ArrayList<DataSetMetabaseVO>();
    for (DesignDatasetVO designDatasetVO : listDesignDatasetVO) {
      String datasetSchemaId =
          datasetSchemaControllerZuul.getDatasetSchemaId(designDatasetVO.getId());
      RulesSchemaVO rulesSchemaVO = rulesControllerZuul.findRuleSchemaByDatasetId(datasetSchemaId);
      listRulesSchemaVO.add(rulesSchemaVO);
      DataSetMetabaseVO datasetMetabaseVO =
          datasetMetabaseControllerZuul.findDatasetMetabaseById(designDatasetVO.getId());
      listDataSetMetabaseVO.add(datasetMetabaseVO);
      // List<UniqueConstraintVO> listConstraintVO =
      // datasetSchemaControllerZuul.getUniqueConstraints(datasetSchemaId, dataflow.getId());
      // integrationControllerZuul.findExtensionsAndOperations(null);
    }



    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    Workbook workbook = new XSSFWorkbook();

    // Sheet Tables and Fields
    Sheet sheet = workbook.createSheet("Tables & Fields");
    // Set headers
    int nColumn = 0;
    int nRow = 0;

    // Title
    Row rowhead = sheet.createRow(nRow);
    for (DesignDatasetVO designDatasetVO : listDesignDatasetVO) {
      rowhead.createCell(nColumn).setCellValue(designDatasetVO.getDataSetName());
      nRow += 2;

      // Tables
      rowhead = sheet.createRow(nRow);
      rowhead.createCell(nColumn).setCellValue("Tables");
      nRow++;
      rowhead = sheet.createRow(nRow);
      rowhead.createCell(nColumn).setCellValue("Name");
      nColumn++;
      rowhead.createCell(nColumn).setCellValue("Description");
      nColumn++;
      rowhead.createCell(nColumn).setCellValue("Read only");
      nColumn++;
      rowhead.createCell(nColumn).setCellValue("Prefilled");
      nColumn++;
      rowhead.createCell(nColumn).setCellValue("Fixed number of records");
      nColumn++;
      rowhead.createCell(nColumn).setCellValue("Mandatory table");
      nColumn = 0;
      nRow++;

      // Fill tables info
      DataSetSchemaVO datasetSchemaVO =
          datasetSchemaControllerZuul.findDataSchemaByDatasetId(designDatasetVO.getId());

      for (TableSchemaVO tableSchemaVO : datasetSchemaVO.getTableSchemas()) {
        rowhead = sheet.createRow(nRow);
        rowhead.createCell(nColumn).setCellValue(tableSchemaVO.getNameTableSchema());
        nColumn++;
        rowhead.createCell(nColumn).setCellValue(tableSchemaVO.getDescription());
        nColumn++;
        rowhead.createCell(nColumn).setCellValue(booleanToYesOrNo(tableSchemaVO.getReadOnly()));
        nColumn++;
        rowhead.createCell(nColumn).setCellValue(booleanToYesOrNo(tableSchemaVO.getToPrefill()));
        nColumn++;
        rowhead.createCell(nColumn).setCellValue(booleanToYesOrNo(tableSchemaVO.getFixedNumber()));
        nColumn++;
        String datasetSchemaId =
            datasetSchemaControllerZuul.getDatasetSchemaId(designDatasetVO.getId());
        RulesSchemaVO rulesSchemaVO =
            rulesControllerZuul.findRuleSchemaByDatasetId(datasetSchemaId);
        String mandatoryTable = "No";
        for (RuleVO ruleVO : rulesSchemaVO.getRules()) {
          if (AutomaticRuleTypeEnum.MANDATORY_TABLE.equals(ruleVO.getAutomaticType())
              && tableSchemaVO.getIdTableSchema().equals(ruleVO.getReferenceId())) {
            mandatoryTable = "Yes";
            break;
          }
        }
        rowhead.createCell(nColumn).setCellValue(mandatoryTable);
        nColumn = 0;
        nRow++;
      }

      // Fields tables
      for (TableSchemaVO tableSchemaVO : datasetSchemaVO.getTableSchemas()) {
        nRow++;
        rowhead = sheet.createRow(nRow);
        rowhead.createCell(nColumn)
            .setCellValue("Fields table " + tableSchemaVO.getNameTableSchema());
        nRow++;
        rowhead = sheet.createRow(nRow);
        rowhead.createCell(nColumn).setCellValue("Primary Key");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Required");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Read only");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Name");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Description");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Type");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Single/multiple select items");
        nColumn++;
        rowhead.createCell(nColumn).setCellValue("Format");
        nColumn = 0;
        nRow++;
        List<FieldSchemaVO> listFieldSchemaVO = tableSchemaVO.getRecordSchema().getFieldSchema();
        for (FieldSchemaVO fieldSchemaVO : listFieldSchemaVO) {
          rowhead = sheet.createRow(nRow);
          rowhead.createCell(nColumn).setCellValue(booleanToYesOrNo(fieldSchemaVO.getPk()));
          nColumn++;
          rowhead.createCell(nColumn).setCellValue(booleanToYesOrNo(fieldSchemaVO.getRequired()));
          nColumn++;
          rowhead.createCell(nColumn).setCellValue(booleanToYesOrNo(fieldSchemaVO.getReadOnly()));
          nColumn++;
          rowhead.createCell(nColumn).setCellValue(fieldSchemaVO.getName());
          nColumn++;
          rowhead.createCell(nColumn).setCellValue(fieldSchemaVO.getDescription());
          nColumn++;
          rowhead.createCell(nColumn).setCellValue(fieldSchemaVO.getType().getValue());
          nColumn++;
          rowhead.createCell(nColumn).setCellValue("...");
          nColumn++;
          rowhead.createCell(nColumn).setCellValue("...");
          nColumn = 0;
          nRow++;
        }
      }
    }

    // Sheet QC rules
    sheet = workbook.createSheet("QC rules");
    // Set headers
    nColumn = 0;
    nRow = 0;

    // Title
    rowhead = sheet.createRow(nRow);

    workbook.write(byteArrayOutputStream);

    return byteArrayOutputStream.toByteArray();
  }

  /**
   * Boolean to yes or no.
   *
   * @param condition the condition
   * @return the string
   */
  private String booleanToYesOrNo(Boolean condition) {
    String result = "Yes";
    if (condition == null || !condition) {
      result = "No";
    }
    return result;
  }
}
