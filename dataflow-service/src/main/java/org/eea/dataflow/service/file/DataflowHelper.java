package org.eea.dataflow.service.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.AutomaticRuleTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataflowHelper.
 */
@Component
public class DataflowHelper {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowHelper.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
  private RulesControllerZuul rulesControllerZuul;

  @Autowired
  private IntegrationControllerZuul integrationControllerZuul;

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
   * Export schema information.
   *
   * @param dataflowId the dataflow id
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Async
  public void exportSchemaInformation(Long dataflowId) throws IOException, EEAException {

    String composedFileName = "dataflow-" + dataflowId + "-Schema_Information";
    String fileNameWithExtension = composedFileName + "_"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")) + "."
        + FileTypeEnum.XLSX.getValue();
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
    String folderName = "dataflow-" + dataflowId + "-Schema_Information";

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
   * Download public schema information.
   *
   * @param dataflowId the dataflow id
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ResponseStatusException the response status exception
   */
  public byte[] downloadPublicSchemaInformation(Long dataflowId)
      throws IOException, ResponseStatusException {
    return writeFile(dataflowId);
  }

  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private byte[] writeFile(Long dataflowId) throws IOException {

    Map<DataType, String> fieldFormat = new HashMap<>();
    fieldFormat.put(DataType.DATE, "YYYY-MM-DD");
    fieldFormat.put(DataType.DATETIME, "YYYY-MM-DD");
    fieldFormat.put(DataType.TEXT, "Maximum of 10000 characters");
    fieldFormat.put(DataType.TEXTAREA, "Maximum of 10000 characters");
    fieldFormat.put(DataType.NUMBER_INTEGER, "Maximum of 20 characters");
    fieldFormat.put(DataType.NUMBER_DECIMAL, "Maximum of 40 characters");
    fieldFormat.put(DataType.EMAIL, "Maximum of 256 characters");
    fieldFormat.put(DataType.PHONE, "Maximum of 256 characters");
    fieldFormat.put(DataType.URL, "Maximum of 10000 characters");
    fieldFormat.put(DataType.POINT, "Check the type format https://geojsonlint.com/");
    fieldFormat.put(DataType.MULTIPOINT, "Check the type format https://geojsonlint.com/");
    fieldFormat.put(DataType.LINESTRING, "Check the type format https://geojsonlint.com/");
    fieldFormat.put(DataType.MULTILINESTRING, "Check the type format https://geojsonlint.com/");
    fieldFormat.put(DataType.POLYGON, "Check the type format https://geojsonlint.com/");
    fieldFormat.put(DataType.MULTIPOLYGON, "Check the type format https://geojsonlint.com/");

    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
    List<DesignDatasetVO> listDesignDatasetVO =
        datasetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId);

    Map<String, String> tableNames = new HashMap<>();
    Map<String, String> fieldNames = new HashMap<>();
    for (DesignDatasetVO designDatasetVO : listDesignDatasetVO) {
      DataSetSchemaVO datasetSchemaVO =
          datasetSchemaControllerZuul.findDataSchemaByDatasetIdPrivate(designDatasetVO.getId());
      for (TableSchemaVO table : datasetSchemaVO.getTableSchemas()) {
        tableNames.put(table.getIdTableSchema(), table.getNameTableSchema());
        for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
          fieldNames.put(field.getId(), field.getName());
          tableNames.put(field.getId(), table.getNameTableSchema());
        }
      }
    }

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    // Create workbook
    try (Workbook workbook = new XSSFWorkbook()) {

      // Create sheets
      Sheet sheetTF = workbook.createSheet("Tables & Fields");
      Sheet sheetQC = workbook.createSheet("QC rules");
      Sheet sheetUN = workbook.createSheet("Uniques");
      Sheet sheetEI = workbook.createSheet("Externa integrations");

      // Set columns and rows
      int nColumnTF = 0;
      int nRowTF = 0;
      int nColumnQC = 0;
      int nRowQC = 0;
      int nColumnUN = 0;
      int nRowUN = 0;
      int nColumnEI = 0;
      int nRowEI = 0;

      for (DesignDatasetVO designDatasetVO : listDesignDatasetVO) {
        // Tables & Fields
        Row rowheadTF;
        rowheadTF = sheetTF.createRow(nRowTF);
        rowheadTF.createCell(nColumnTF).setCellValue(designDatasetVO.getDataSetName());
        nRowTF += 2;

        // Tables
        rowheadTF = sheetTF.createRow(nRowTF);
        rowheadTF.createCell(nColumnTF).setCellValue("Tables");
        nRowTF++;
        rowheadTF = sheetTF.createRow(nRowTF);
        rowheadTF.createCell(nColumnTF).setCellValue("Name");
        nColumnTF++;
        rowheadTF.createCell(nColumnTF).setCellValue("Description");
        nColumnTF++;
        rowheadTF.createCell(nColumnTF).setCellValue("Read only");
        nColumnTF++;
        rowheadTF.createCell(nColumnTF).setCellValue("Prefilled");
        nColumnTF++;
        rowheadTF.createCell(nColumnTF).setCellValue("Fixed number of records");
        nColumnTF++;
        rowheadTF.createCell(nColumnTF).setCellValue("Mandatory table");
        nColumnTF = 0;
        nRowTF++;

        // Fill tables info
        DataSetSchemaVO datasetSchemaVO =
            datasetSchemaControllerZuul.findDataSchemaByDatasetIdPrivate(designDatasetVO.getId());

        for (TableSchemaVO tableSchemaVO : datasetSchemaVO.getTableSchemas()) {
          rowheadTF = sheetTF.createRow(nRowTF);
          rowheadTF.createCell(nColumnTF).setCellValue(tableSchemaVO.getNameTableSchema());
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue(tableSchemaVO.getDescription());
          nColumnTF++;
          rowheadTF.createCell(nColumnTF)
              .setCellValue(booleanToYesOrNo(tableSchemaVO.getReadOnly()));
          nColumnTF++;
          rowheadTF.createCell(nColumnTF)
              .setCellValue(booleanToYesOrNo(tableSchemaVO.getToPrefill()));
          nColumnTF++;
          rowheadTF.createCell(nColumnTF)
              .setCellValue(booleanToYesOrNo(tableSchemaVO.getFixedNumber()));
          nColumnTF++;
          String datasetSchemaId =
              datasetSchemaControllerZuul.getDatasetSchemaId(designDatasetVO.getId());
          RulesSchemaVO rulesSchemaVO =
              rulesControllerZuul.findRuleSchemaByDatasetIdPrivate(datasetSchemaId, dataflowId);
          String mandatoryTable = "No";
          for (RuleVO ruleVO : rulesSchemaVO.getRules()) {
            if (AutomaticRuleTypeEnum.MANDATORY_TABLE.equals(ruleVO.getAutomaticType())
                && tableSchemaVO.getIdTableSchema().equals(ruleVO.getReferenceId())) {
              mandatoryTable = "Yes";
              break;
            }
          }
          rowheadTF.createCell(nColumnTF).setCellValue(mandatoryTable);
          nColumnTF = 0;
          nRowTF++;
        }

        // Fields tables
        for (TableSchemaVO tableSchemaVO : datasetSchemaVO.getTableSchemas()) {
          nRowTF++;
          rowheadTF = sheetTF.createRow(nRowTF);
          rowheadTF.createCell(nColumnTF)
              .setCellValue("Fields table " + tableSchemaVO.getNameTableSchema());
          nRowTF++;
          rowheadTF = sheetTF.createRow(nRowTF);
          rowheadTF.createCell(nColumnTF).setCellValue("Primary Key");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Required");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Read only");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Name");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Description");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Type");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Single/multiple select items");
          nColumnTF++;
          rowheadTF.createCell(nColumnTF).setCellValue("Format");
          nColumnTF = 0;
          nRowTF++;
          List<FieldSchemaVO> listFieldSchemaVO = tableSchemaVO.getRecordSchema().getFieldSchema();
          for (FieldSchemaVO fieldSchemaVO : listFieldSchemaVO) {
            rowheadTF = sheetTF.createRow(nRowTF);
            rowheadTF.createCell(nColumnTF).setCellValue(booleanToYesOrNo(fieldSchemaVO.getPk()));
            nColumnTF++;
            rowheadTF.createCell(nColumnTF)
                .setCellValue(booleanToYesOrNo(fieldSchemaVO.getRequired()));
            nColumnTF++;
            rowheadTF.createCell(nColumnTF)
                .setCellValue(booleanToYesOrNo(fieldSchemaVO.getReadOnly()));
            nColumnTF++;
            rowheadTF.createCell(nColumnTF).setCellValue(fieldSchemaVO.getName());
            nColumnTF++;
            rowheadTF.createCell(nColumnTF).setCellValue(fieldSchemaVO.getDescription());
            nColumnTF++;
            rowheadTF.createCell(nColumnTF).setCellValue(fieldSchemaVO.getType().getValue());
            nColumnTF++;
            String codeListItems = "";
            if (fieldSchemaVO.getCodelistItems() != null) {
              codeListItems = String.join(",", fieldSchemaVO.getCodelistItems());
            }
            rowheadTF.createCell(nColumnTF).setCellValue(codeListItems);
            nColumnTF++;
            String format = "";
            if (DataType.ATTACHMENT.equals(fieldSchemaVO.getType())) {
              String validExtensions = "";
              if (fieldSchemaVO.getCodelistItems() != null) {
                validExtensions = String.join(",", fieldSchemaVO.getValidExtensions());
              }
              format = "Valid extension: " + validExtensions + " - Max file size: "
                  + fieldSchemaVO.getMaxSize() + " MB";
            } else {
              format = fieldFormat.get(fieldSchemaVO.getType());
            }
            rowheadTF.createCell(nColumnTF).setCellValue(format);
            nColumnTF = 0;
            nRowTF++;
          }
        }
        nRowTF += 2;

        // QC rules
        String datasetSchemaId =
            datasetSchemaControllerZuul.getDatasetSchemaId(designDatasetVO.getId());
        RulesSchemaVO rulesSchemaVO =
            rulesControllerZuul.findRuleSchemaByDatasetIdPrivate(datasetSchemaId, dataflowId);

        Row rowheadQC = sheetQC.createRow(nRowQC);
        rowheadQC.createCell(nColumnQC).setCellValue(designDatasetVO.getDataSetName());
        nRowQC += 2;

        // Table QC rules
        rowheadQC = sheetQC.createRow(nRowQC);
        rowheadQC.createCell(nColumnQC).setCellValue("Table");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Field");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Shortcode");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Name");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Description");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Expression");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Type of QC");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Level error");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Message");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Automatic");
        nColumnQC++;
        rowheadQC.createCell(nColumnQC).setCellValue("Enabled");
        nColumnQC = 0;
        nRowQC++;

        // Fill QC rules table info
        for (RuleVO ruleVO : rulesSchemaVO.getRules()) {
          rowheadQC = sheetQC.createRow(nRowQC);
          rowheadQC.createCell(nColumnQC)
              .setCellValue(tableNames.containsKey(ruleVO.getReferenceId())
                  ? tableNames.get(ruleVO.getReferenceId())
                  : "");
          nColumnQC++;
          rowheadQC.createCell(nColumnQC)
              .setCellValue(fieldNames.containsKey(ruleVO.getReferenceId())
                  ? fieldNames.get(ruleVO.getReferenceId())
                  : "");
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getShortCode());
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getRuleName());
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getDescription());
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getExpressionText());
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getType().getValue());
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getThenCondition().get(1));
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(ruleVO.getThenCondition().get(0));
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(booleanToYesOrNo(ruleVO.isAutomatic()));
          nColumnQC++;
          rowheadQC.createCell(nColumnQC).setCellValue(booleanToYesOrNo(ruleVO.isEnabled()));
          nColumnQC = 0;
          nRowQC++;
        }
        nRowQC += 2;

        // Sheet Uniques
        Row rowheadUN = sheetUN.createRow(nRowUN);
        rowheadUN.createCell(nColumnUN).setCellValue(designDatasetVO.getDataSetName());
        nRowUN += 2;

        // Table Uniques
        rowheadUN = sheetUN.createRow(nRowUN);
        rowheadUN.createCell(nColumnUN).setCellValue("Table");
        nColumnUN++;
        rowheadUN.createCell(nColumnUN).setCellValue("Field");
        nColumnUN = 0;
        nRowUN++;

        // Fill uniques tables info
        List<UniqueConstraintVO> listConstraintVO = datasetSchemaControllerZuul
            .getPublicUniqueConstraints(datasetSchemaVO.getIdDataSetSchema(), dataflow.getId());
        for (UniqueConstraintVO uniqueConstraintVO : listConstraintVO) {
          for (String fieldSchemaId : uniqueConstraintVO.getFieldSchemaIds()) {
            rowheadUN = sheetUN.createRow(nRowUN);
            rowheadUN.createCell(nColumnUN)
                .setCellValue(tableNames.containsKey(uniqueConstraintVO.getTableSchemaId())
                    ? tableNames.get(uniqueConstraintVO.getTableSchemaId())
                    : "");
            nColumnUN++;
            rowheadUN.createCell(nColumnUN).setCellValue(
                fieldNames.containsKey(fieldSchemaId) ? fieldNames.get(fieldSchemaId) : "");
            nColumnUN = 0;
            nRowUN++;
          }
        }
        nRowUN += 2;

        // External Integrations
        Row rowheadEI = sheetEI.createRow(nRowEI);
        rowheadEI.createCell(nColumnEI).setCellValue(designDatasetVO.getDataSetName());
        nRowEI += 2;

        // Table external integrations
        rowheadEI = sheetEI.createRow(nRowEI);
        rowheadEI.createCell(nColumnEI).setCellValue("Dataset");
        nColumnEI++;
        rowheadEI.createCell(nColumnEI).setCellValue("Operation");
        nColumnEI++;
        rowheadEI.createCell(nColumnEI).setCellValue("Extension/s");
        nColumnEI++;
        rowheadEI.createCell(nColumnEI).setCellValue("Id");
        nColumnEI = 0;
        nRowEI++;

        // Fill external integrations tables info
        IntegrationVO integrationVO = new IntegrationVO();
        LinkedHashMap<String, String> internalParameters = new LinkedHashMap();
        internalParameters.put("dataflowId", dataflowId.toString());
        internalParameters.put("datasetSchemaId", datasetSchemaId);
        integrationVO.setInternalParameters(internalParameters);
        List<IntegrationVO> listIntegrationVO =
            integrationControllerZuul.findExtensionsAndOperationsPrivate(integrationVO);
        for (IntegrationVO integration : listIntegrationVO) {
          rowheadEI = sheetEI.createRow(nRowEI);
          rowheadEI.createCell(nColumnEI).setCellValue(designDatasetVO.getDataSetName());
          nColumnEI++;
          rowheadEI.createCell(nColumnEI).setCellValue(integration.getOperation().getValue());
          nColumnEI++;
          rowheadEI.createCell(nColumnEI)
              .setCellValue(integration.getInternalParameters().get("fileExtension"));
          nColumnEI++;
          rowheadEI.createCell(nColumnEI).setCellValue(integration.getId());
          nColumnEI = 0;
          nRowEI++;
        }
        nRowEI += 2;
      }

      workbook.write(byteArrayOutputStream);
    }

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
