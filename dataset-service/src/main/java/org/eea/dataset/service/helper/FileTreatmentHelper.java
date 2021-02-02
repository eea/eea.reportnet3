package org.eea.dataset.service.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.model.ImportSchemas;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class FileTreatmentHelper implements DisposableBean {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FileTreatmentHelper.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The max running tasks. */
  @Value("${dataset.task.parallelism}")
  private int maxRunningTasks;

  /** The import path. */
  @Value("${importPath}")
  private String importPath;

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The import executor service. */
  private ExecutorService importExecutorService;


  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The unique constraint repository. */
  @Autowired
  private UniqueConstraintRepository uniqueConstraintRepository;

  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /** The integration controller zuul. */
  @Autowired
  private IntegrationControllerZuul integrationControllerZuul;


  /** The batch size. */
  private int batchSize = 1000;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    importExecutorService = Executors.newFixedThreadPool(maxRunningTasks);
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
   * Import file data.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   * @throws EEAException the EEA exception
   */
  public void importFileData(Long datasetId, String tableSchemaId, MultipartFile file,
      boolean replace) throws EEAException {
    DataSetSchema schema = datasetService.getSchemaIfReportable(datasetId, tableSchemaId);
    if (null == schema) {
      datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
      LOG_ERROR.error("Dataset not reportable: datasetId={}, tableSchemaId={}, fileName={}",
          datasetId, tableSchemaId, file.getName());
      throw new EEAException(
          "Dataset not reportable: datasetId=" + datasetId + ", tableSchemaId=" + tableSchemaId);
    }
    fileManagement(datasetId, tableSchemaId, schema, file, replace);
  }



  /**
   * Un zip import schema.
   *
   * @param multipartFile the multipart file
   * @return the import schemas
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ImportSchemas unZipImportSchema(MultipartFile multipartFile)
      throws EEAException, IOException {

    ImportSchemas fileUnziped = new ImportSchemas();
    try (InputStream input = multipartFile.getInputStream()) {
      String fileName = multipartFile.getOriginalFilename();
      String multipartFileMimeType = datasetService.getMimetype(fileName);

      List<DataSetSchema> schemas = new ArrayList<>();
      Map<String, String> schemaNames = new HashMap<>();
      List<IntegrationVO> extIntegrations = new ArrayList<>();
      List<UniqueConstraintSchema> uniques = new ArrayList<>();
      List<IntegrityVO> integrities = new ArrayList<>();
      List<byte[]> qcrulesBytes = new ArrayList<>();

      if ("zip".equalsIgnoreCase(multipartFileMimeType)) {
        try (ZipInputStream zip = new ZipInputStream(input)) {
          for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {

            String entryName = entry.getName();
            String mimeType = datasetService.getMimetype(entryName);
            switch (mimeType.toLowerCase()) {
              case "schema":
                schemas = unzippingSchemaClasses(zip, schemas);
                break;
              case "qcrules":
                qcrulesBytes = unzippingQcClasses(zip, qcrulesBytes);
                break;
              case "unique":
                uniques = unzippingUniqueClasses(zip, uniques);
                break;
              case "names":
                schemaNames = unzippingDatasetNamesClasses(zip, schemaNames);
                break;
              case "extintegrations":
                extIntegrations = unzippingExtIntegrationsClasses(zip, extIntegrations);
                break;
              case "integrity":
                integrities = unzippingIntegrityQcClasses(zip, integrities);
                break;
              default:
                break;
            }
          }
          zip.closeEntry();

          fileUnziped.setSchemaNames(schemaNames);
          fileUnziped.setSchemas(schemas);
          fileUnziped.setUniques(uniques);
          fileUnziped.setExternalIntegrations(extIntegrations);
          fileUnziped.setQcRulesBytes(qcrulesBytes);
          fileUnziped.setIntegrities(integrities);
        }
      }
    }
    return fileUnziped;
  }


  /**
   * Zip schema.
   *
   * @param designs the designs
   * @param schemas the schemas
   * @param dataflowId the dataflow id
   * @return the byte[]
   */
  public byte[] zipSchema(List<DesignDataset> designs, List<DataSetSchema> schemas,
      Long dataflowId) {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Map<String, String> schemaNames = new HashMap<>();
    try (ZipOutputStream zos = new ZipOutputStream(bos)) {
      for (DataSetSchema schema : schemas) {

        // Put the datasetSchemaId and the dataset name into a map to store later in the zip file
        DesignDataset design = designs.stream()
            .filter(d -> d.getDatasetSchema().equals(schema.getIdDataSetSchema().toString()))
            .findFirst().orElse(new DesignDataset());
        schemaNames.put(schema.getIdDataSetSchema().toString(), design.getDataSetName());

        // Schemas
        zipSchemaClasses(schema, zos, design.getDataSetName());

        // Rules
        zipRuleClasses(schema, zos, design.getDataSetName());

        // Unique
        zipUniqueClasses(schema, zos, design.getDataSetName());

        // Integrity
        zipIntegrityClasses(schema, zos, design.getDataSetName());

        // Store the external integration
        zipExternalIntegrationClasses(schema, dataflowId, zos, design.getDataSetName());
      }
      // Store the dataset names
      zipDatasetNames(schemaNames, zos);

    } catch (Exception e) {
      LOG.error("Error exporting schemas to a ZIP file {}", e.getMessage(), e);
    }
    return bos.toByteArray();

  }


  /**
   * Zip schema classes.
   *
   * @param schema the schema
   * @param zos the zos
   * @param fileName the file name
   */
  private void zipSchemaClasses(DataSetSchema schema, ZipOutputStream zos, String fileName) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String nameFile = fileName + ".schema";
      InputStream schemaStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(schema));
      ZipEntry zeSchem = new ZipEntry(nameFile);
      zos.putNextEntry(zeSchem);
      byte[] bytes = new byte[1024];
      int count = schemaStream.read(bytes);
      while (count > -1) {
        zos.write(bytes, 0, count);
        count = schemaStream.read(bytes);
      }
      schemaStream.close();
    } catch (IOException e) {
      LOG.error("Error exporting the schema into the zip. {}", e.getMessage(), e);
    }
  }

  /**
   * Zip rule classes.
   *
   * @param schema the schema
   * @param zos the zos
   * @param fileName the file name
   */
  private void zipRuleClasses(DataSetSchema schema, ZipOutputStream zos, String fileName) {
    try {
      RulesSchema rules = rulesRepository.findByIdDatasetSchema(schema.getIdDataSetSchema());
      ObjectMapper objectMapperRules = new ObjectMapper();
      String nameFileRules = fileName + ".qcrules";
      InputStream rulesStream =
          new ByteArrayInputStream(objectMapperRules.writeValueAsBytes(rules));
      ZipEntry zeRules = new ZipEntry(nameFileRules);
      zos.putNextEntry(zeRules);
      byte[] bytesRules = new byte[1024];
      int countRules = rulesStream.read(bytesRules);
      while (countRules > -1) {
        zos.write(bytesRules, 0, countRules);
        countRules = rulesStream.read(bytesRules);
      }
      rulesStream.close();
    } catch (IOException e) {
      LOG.error("Error exporting the qcRules into the zip. {}", e.getMessage(), e);
    }
  }

  /**
   * Zip unique classes.
   *
   * @param schema the schema
   * @param zos the zos
   * @param fileName the file name
   */
  private void zipUniqueClasses(DataSetSchema schema, ZipOutputStream zos, String fileName) {
    try {
      List<UniqueConstraintSchema> listUnique =
          uniqueConstraintRepository.findByDatasetSchemaId(schema.getIdDataSetSchema());
      ObjectMapper objectMapperUnique = new ObjectMapper();
      String nameFileUnique = fileName + ".unique";
      InputStream uniqueStream =
          new ByteArrayInputStream(objectMapperUnique.writeValueAsBytes(listUnique));
      ZipEntry zeUnique = new ZipEntry(nameFileUnique);
      zos.putNextEntry(zeUnique);
      byte[] bytesUnique = new byte[1024];
      int countUnique = uniqueStream.read(bytesUnique);
      while (countUnique > -1) {
        zos.write(bytesUnique, 0, countUnique);
        countUnique = uniqueStream.read(bytesUnique);
      }
      uniqueStream.close();
    } catch (IOException e) {
      LOG.error("Error exporting the unique rules into the zip. {}", e.getMessage(), e);
    }
  }

  /**
   * Zip integrity classes.
   *
   * @param schema the schema
   * @param zos the zos
   * @param fileName the file name
   */
  private void zipIntegrityClasses(DataSetSchema schema, ZipOutputStream zos, String fileName) {
    try {
      List<IntegrityVO> listIntegrity = rulesControllerZuul
          .getIntegrityRulesByDatasetSchemaId(schema.getIdDataSetSchema().toString());
      ObjectMapper objectMapperIntegrity = new ObjectMapper();
      String nameFileIntegrity = fileName + ".integrity";
      InputStream integrityStream =
          new ByteArrayInputStream(objectMapperIntegrity.writeValueAsBytes(listIntegrity));
      ZipEntry zeIntegrity = new ZipEntry(nameFileIntegrity);
      zos.putNextEntry(zeIntegrity);
      byte[] bytesIntegrity = new byte[1024];
      int countIntegrity = integrityStream.read(bytesIntegrity);
      while (countIntegrity > -1) {
        zos.write(bytesIntegrity, 0, countIntegrity);
        countIntegrity = integrityStream.read(bytesIntegrity);
      }
      integrityStream.close();
    } catch (IOException e) {
      LOG.error("Error exporting the integrity rules into the zip. {}", e.getMessage(), e);
    }
  }

  /**
   * Zip external integration classes.
   *
   * @param schema the schema
   * @param dataflowId the dataflow id
   * @param zos the zos
   * @param fileName the file name
   */
  private void zipExternalIntegrationClasses(DataSetSchema schema, Long dataflowId,
      ZipOutputStream zos, String fileName) {
    try {
      IntegrationVO integration = new IntegrationVO();
      Map<String, String> internalParameters = new HashMap<>();
      internalParameters.put("dataflowId", dataflowId.toString());
      internalParameters.put("datasetSchemaId", schema.getIdDataSetSchema().toString());
      integration.setInternalParameters(internalParameters);
      List<IntegrationVO> extIntegrations =
          integrationControllerZuul.findAllIntegrationsByCriteria(integration);
      ObjectMapper objectMapperIntegration = new ObjectMapper();
      InputStream extIntegrationStream =
          new ByteArrayInputStream(objectMapperIntegration.writeValueAsBytes(extIntegrations));
      String nameFileExtIntegrations = fileName + ".extintegrations";
      ZipEntry zeIntegration = new ZipEntry(nameFileExtIntegrations);
      zos.putNextEntry(zeIntegration);
      byte[] bytesIntegration = new byte[1024];
      int countIntegration = extIntegrationStream.read(bytesIntegration);
      while (countIntegration > -1) {
        zos.write(bytesIntegration, 0, countIntegration);
        countIntegration = extIntegrationStream.read(bytesIntegration);
      }
      extIntegrationStream.close();
    } catch (IOException e) {
      LOG.error("Error exporting the external integrations into the zip. {}", e.getMessage(), e);
    }
  }


  /**
   * Zip dataset names.
   *
   * @param schemaNames the schema names
   * @param zos the zos
   */
  private void zipDatasetNames(Map<String, String> schemaNames, ZipOutputStream zos) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      InputStream schemaNamesStream =
          new ByteArrayInputStream(objectMapper.writeValueAsBytes(schemaNames));
      ZipEntry ze = new ZipEntry("datasetSchemaNames.names");
      zos.putNextEntry(ze);
      byte[] bytes = new byte[1024];
      int count = schemaNamesStream.read(bytes);
      while (count > -1) {
        zos.write(bytes, 0, count);
        count = schemaNamesStream.read(bytes);
      }
      schemaNamesStream.close();
    } catch (IOException e) {
      LOG.error("Error exporting the dataset names into the zip. {}", e.getMessage(), e);
    }
  }


  /**
   * Unzipping schema classes.
   *
   * @param zip the zip
   * @param schemas the schemas
   * @return the list
   */
  private List<DataSetSchema> unzippingSchemaClasses(ZipInputStream zip,
      List<DataSetSchema> schemas) {

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = zip.read(buf, 0, 1024)) != -1) {
        output.write(buf, 0, n);
      }
      byte[] content = output.toByteArray();
      if (content != null && content.length > 0) {
        DataSetSchema schema = objectMapper.readValue(content, DataSetSchema.class);
        LOG.info("Schema class recovered from zip file");
        schemas.add(schema);
      }
    } catch (Exception e) {

    }
    return schemas;
  }


  /**
   * Unzipping qc classes.
   *
   * @param zip the zip
   * @param qcrulesBytes the qcrules bytes
   * @return the list
   */
  private List<byte[]> unzippingQcClasses(ZipInputStream zip, List<byte[]> qcrulesBytes) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = zip.read(buf, 0, 1024)) != -1) {
        output.write(buf, 0, n);
      }
      byte[] content = output.toByteArray();

      if (content != null && content.length > 0) {
        LOG.info("QcRule class recovered from zip file");
        qcrulesBytes.add(content);
      }
    } catch (Exception e) {

    }
    return qcrulesBytes;
  }

  /**
   * Unzipping unique classes.
   *
   * @param zip the zip
   * @param uniques the uniques
   * @return the list
   */
  private List<UniqueConstraintSchema> unzippingUniqueClasses(ZipInputStream zip,
      List<UniqueConstraintSchema> uniques) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = zip.read(buf, 0, 1024)) != -1) {
        output.write(buf, 0, n);
      }
      byte[] content = output.toByteArray();
      if (content != null && content.length > 0) {
        uniques
            .addAll(Arrays.asList(objectMapper.readValue(content, UniqueConstraintSchema[].class)));
        LOG.info("Unique class recovered from zip file");
      }
    } catch (Exception e) {

    }
    return uniques;
  }


  /**
   * Unzipping dataset names classes.
   *
   * @param zip the zip
   * @param schemaNames the schema names
   * @return the map
   */
  private Map<String, String> unzippingDatasetNamesClasses(ZipInputStream zip,
      Map<String, String> schemaNames) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = zip.read(buf, 0, 1024)) != -1) {
        output.write(buf, 0, n);
      }
      byte[] content = output.toByteArray();
      if (content != null && content.length > 0) {
        schemaNames = objectMapper.readValue(content, Map.class);
        LOG.info("Schema names recovered from zip file");
      }
    } catch (Exception e) {

    }
    return schemaNames;
  }

  /**
   * Unzipping ext integrations classes.
   *
   * @param zip the zip
   * @param extIntegrations the ext integrations
   * @return the list
   */
  private List<IntegrationVO> unzippingExtIntegrationsClasses(ZipInputStream zip,
      List<IntegrationVO> extIntegrations) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = zip.read(buf, 0, 1024)) != -1) {
        output.write(buf, 0, n);
      }
      byte[] content = output.toByteArray();
      if (content != null && content.length > 0) {
        extIntegrations
            .addAll(Arrays.asList(objectMapper.readValue(content, IntegrationVO[].class)));
        LOG.info("External integration recovered from zip file");
      }
    } catch (Exception e) {

    }
    return extIntegrations;
  }

  /**
   * Unzipping integrity qc classes.
   *
   * @param zip the zip
   * @param integrities the integrities
   * @return the list
   */
  private List<IntegrityVO> unzippingIntegrityQcClasses(ZipInputStream zip,
      List<IntegrityVO> integrities) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = zip.read(buf, 0, 1024)) != -1) {
        output.write(buf, 0, n);
      }
      byte[] content = output.toByteArray();
      if (content != null && content.length > 0) {
        integrities.addAll(Arrays.asList(objectMapper.readValue(content, IntegrityVO[].class)));
        LOG.info("External integration recovered from zip file");
      }
    } catch (Exception e) {

    }
    return integrities;
  }



  /**
   * Release lock.
   *
   * @param datasetId the dataset id
   */
  private void releaseLock(Long datasetId) {
    try {
      datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
      FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));
    } catch (IOException e) {
      LOG_ERROR.error("Error deleting files: datasetId={}", datasetId, e);
    }
  }

  /**
   * File management.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param multipartFile the multipart file
   * @param delete the delete
   * @throws EEAException the EEA exception
   */
  private void fileManagement(Long datasetId, String tableSchemaId, DataSetSchema schema,
      MultipartFile multipartFile, boolean delete) throws EEAException {

    try (InputStream input = multipartFile.getInputStream()) {

      // Prepare the folder where files will be stored
      File root = new File(importPath);
      File folder = new File(root, datasetId.toString());
      String saveLocationPath = folder.getCanonicalPath();
      String originalFileName = multipartFile.getOriginalFilename();
      String multipartFileMimeType = datasetService.getMimetype(originalFileName);

      if (!folder.mkdirs()) {
        releaseLock(datasetId);
        throw new EEAException("Folder for dataset " + datasetId + " already exists");
      }

      if ("zip".equalsIgnoreCase(multipartFileMimeType)) {

        try (ZipInputStream zip = new ZipInputStream(input)) {

          /*
           * TODO. Since ZIP and CSV files are temporally disabled to be imported from FME, we do
           * not need to look for a matching integration.
           */

          // IntegrationVO integrationVO = getIntegrationVO(schema, "csv");
          IntegrationVO integrationVO = null;

          List<File> files = unzipAndStore(folder, saveLocationPath, zip);

          // Queue import tasks for stored files
          if (!files.isEmpty()) {
            wipeData(datasetId, null, delete);
            IntegrationVO copyIntegrationVO = integrationVOCopyConstructor(integrationVO);
            queueImportProcess(datasetId, null, schema, files, originalFileName, copyIntegrationVO);
          } else {
            releaseLock(datasetId);
            throw new EEAException("Empty zip file");
          }
        }

      } else {

        File file = new File(folder, originalFileName);
        List<File> files = new ArrayList<>();

        /*
         * TOOD. Since ZIP and CSV files are temporally disabled to be imported from FME, we do not
         * need to look for a matching integration.
         */

        IntegrationVO integrationVO;
        if ("csv".equalsIgnoreCase(multipartFileMimeType)) {
          integrationVO = null;
        } else {
          // Look for an integration for the given kind of file.
          integrationVO = getIntegrationVO(schema, multipartFileMimeType);
        }

        // Store the file in the persistence volume
        try (FileOutputStream output = new FileOutputStream(file)) {
          IOUtils.copyLarge(input, output);
          files.add(file);
          LOG.info("Stored file {}", file.getPath());
        }

        // Queue import task for the stored file
        wipeData(datasetId, tableSchemaId, delete);
        queueImportProcess(datasetId, tableSchemaId, schema, files, originalFileName,
            integrationVO);
      }

    } catch (FeignException | IOException e) {
      LOG_ERROR.error("Unexpected exception importing file data: datasetId={}, file={}", datasetId,
          multipartFile.getName(), e);
      releaseLock(datasetId);
      throw new EEAException(e);
    }
  }

  /**
   * Unzip and store.
   *
   * @param folder the folder
   * @param saveLocationPath the save location path
   * @param zip the zip
   * @return the list
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private List<File> unzipAndStore(File folder, String saveLocationPath, ZipInputStream zip)
      throws EEAException, IOException {

    List<File> files = new ArrayList<>();
    ZipEntry entry = zip.getNextEntry();

    while (null != entry) {
      String entryName = entry.getName();
      String mimeType = datasetService.getMimetype(entryName);
      File file = new File(folder, entryName);
      String filePath = file.getCanonicalPath();

      // Prevent Zip Slip attack or skip if the entry is a directory
      if (!"csv".equalsIgnoreCase(mimeType) || entry.isDirectory()
          || !filePath.startsWith(saveLocationPath + File.separator)) {
        LOG_ERROR.error("Ignored file from ZIP: {}", entryName);
        entry = zip.getNextEntry();
        continue;
      }

      // Store the file in the persistence volume
      try (FileOutputStream output = new FileOutputStream(file)) {
        IOUtils.copyLarge(zip, output);
        LOG.info("Stored file {}", file.getPath());
      }

      files.add(file);
      entry = zip.getNextEntry();
    }

    return files;
  }

  /**
   * Queue import process.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param files the files
   * @param originalFileName the original file name
   * @param integrationVO the integration VO
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   * @throws FeignException the feign exception
   */
  private void queueImportProcess(Long datasetId, String tableSchemaId, DataSetSchema schema,
      List<File> files, String originalFileName, IntegrationVO integrationVO)
      throws IOException, EEAException {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    if (null != integrationVO) {
      fmeFileProcess(datasetId, files.get(0), integrationVO);
    } else {
      importExecutorService.submit(
          () -> rn3FileProcess(datasetId, tableSchemaId, schema, files, originalFileName, user));
    }
  }

  /**
   * Fme file process.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param integrationVO the integration VO
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   * @throws FeignException the feign exception
   */
  private void fmeFileProcess(Long datasetId, File file, IntegrationVO integrationVO)
      throws IOException, EEAException {

    LOG.info("Start FME-Import process: datasetId={}, integrationVO={}", datasetId, integrationVO);
    boolean error = false;

    try (InputStream inputStream = new FileInputStream(file)) {
      // TODO. Encode and copy the file content into the IntegrationVO. This method load the entire
      // file in memory. To solve it, the FME connector should be redesigned.
      byte[] byteArray = IOUtils.toByteArray(inputStream);
      String encodedString = Base64.getEncoder().encodeToString(byteArray);
      Map<String, String> internalParameters = integrationVO.getInternalParameters();
      Map<String, String> externalParameters = new HashMap<>();
      externalParameters.put("fileIS", encodedString);
      integrationVO.setExternalParameters(externalParameters);

      // Remove the lock so FME will not encounter it while calling back importFileData
      if (!"true".equals(internalParameters.get(IntegrationParams.NOTIFICATION_REQUIRED))) {
        datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
      }

      if ((Integer) integrationController
          .executeIntegrationProcess(IntegrationToolTypeEnum.FME,
              IntegrationOperationTypeEnum.IMPORT, file.getName(), datasetId, integrationVO)
          .getExecutionResultParams().get("id") == 0) {
        error = true;
      }
    }

    FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));

    if (error) {
      LOG_ERROR.error("Error executing integration: datasetId={}, fileName={}, IntegrationVO={}",
          datasetId, file.getName(), integrationVO);
      datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
      throw new EEAException("Error executing integration");
    }
  }

  /**
   * Rn 3 file process.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param files the files
   * @param originalFileName the original file name
   * @param user the user
   */
  private void rn3FileProcess(Long datasetId, String tableSchemaId, DataSetSchema schema,
      List<File> files, String originalFileName, String user) {

    LOG.info("Start RN3-Import process: datasetId={}, files={}", datasetId, files);

    String error = null;

    for (File file : files) {
      String fileName = file.getName();

      try (InputStream inputStream = new FileInputStream(file)) {

        if (files.size() > 1) {
          tableSchemaId = getTableSchemaIdFromFileName(schema, fileName);
        }

        LOG.info("Start RN3-Import file: fileName={}, tableSchemaId={}", fileName, tableSchemaId);

        DataSetVO datasetVO =
            datasetService.processFile(datasetId, fileName, inputStream, tableSchemaId);
        datasetVO.setId(datasetId);
        DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
        if (dataset == null) {
          throw new EEAException("Error processing file " + fileName);
        }

        // Save empty table
        List<RecordValue> allRecords = dataset.getTableValues().get(0).getRecords();
        dataset.getTableValues().get(0).setRecords(new ArrayList<>());

        // Check if the table with idTableSchema has been populated already
        Long oldTableId = datasetService.findTableIdByTableSchema(datasetId, tableSchemaId);
        fillTableId(tableSchemaId, dataset.getTableValues(), oldTableId);

        if (null == oldTableId) {
          datasetService.saveTable(datasetId, dataset.getTableValues().get(0));
        }

        LOG.info("Inserting {} records into database for dataset {} coming from file {}",
            allRecords.size(), datasetId, fileName);
        getListOfRecords(allRecords).parallelStream()
            .forEach(recordValues -> datasetService.saveAllRecords(datasetId, recordValues));

        LOG.info("Finish RN3-Import file: fileName={}, tableSchemaId={}", fileName, tableSchemaId);
      } catch (IOException | EEAException e) {
        LOG_ERROR.error("RN3-Import file failed: fileName={}, tableSchemaId={}", fileName,
            tableSchemaId, e);
        error = e.getMessage();
      }
    }

    if (files.size() == 1) {
      finishImportProcess(datasetId, tableSchemaId, originalFileName, user, error);
    } else {
      finishImportProcess(datasetId, null, originalFileName, user, error);
    }

  }

  /**
   * Gets the table schema id from file name.
   *
   * @param schema the schema
   * @param fileName the file name
   * @return the table schema id from file name
   * @throws EEAException the EEA exception
   */
  private String getTableSchemaIdFromFileName(DataSetSchema schema, String fileName)
      throws EEAException {

    String tableName = fileName.substring(0, fileName.lastIndexOf((".")));
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (tableSchema.getNameTableSchema().equalsIgnoreCase(tableName)) {
        return tableSchema.getIdTableSchema().toString();
      }
    }

    throw new EEAException("File name does not match any table name");
  }

  /**
   * Finish import process conditionally.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param originalFileName the original file name
   * @param user the user
   * @param error the error
   */
  private void finishImportProcess(Long datasetId, String tableSchemaId, String originalFileName,
      String user, String error) {
    try {

      releaseLock(datasetId);

      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      value.put(LiteralConstants.USER, user);

      NotificationVO notificationVO = NotificationVO.builder().user(user).datasetId(datasetId)
          .tableSchemaId(tableSchemaId).fileName(originalFileName).error(error).build();

      EventType eventType;

      if (null != error) {
        eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
            ? EventType.IMPORT_REPORTING_FAILED_EVENT
            : EventType.IMPORT_DESIGN_FAILED_EVENT;
      } else {
        eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
            ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
            : EventType.IMPORT_DESIGN_COMPLETED_EVENT;
        kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, value);
      }

      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("RN3-Import file error", e);
    }
  }

  /**
   * Integration VO copy constructor.
   *
   * @param integrationVO the integration VO
   * @return the integration VO
   */
  private IntegrationVO integrationVOCopyConstructor(IntegrationVO integrationVO) {

    IntegrationVO rtn = null;

    if (null != integrationVO) {
      Map<String, String> oldInternalParameters = integrationVO.getInternalParameters();
      Map<String, String> newInternalParameters = new HashMap<>();
      for (Map.Entry<String, String> entry : oldInternalParameters.entrySet()) {
        newInternalParameters.put(entry.getKey(), entry.getValue());
      }

      rtn = new IntegrationVO();
      rtn.setId(integrationVO.getId());
      rtn.setName(integrationVO.getName());
      rtn.setDescription(integrationVO.getDescription());
      rtn.setTool(integrationVO.getTool());
      rtn.setOperation(integrationVO.getOperation());
      rtn.setInternalParameters(newInternalParameters);
    }

    return rtn;
  }

  /**
   * Gets the integration VO.
   *
   * @param datasetSchema the dataset schema
   * @param mimeType the mime type
   * @return the integration VO
   */
  private IntegrationVO getIntegrationVO(DataSetSchema datasetSchema, String mimeType) {

    IntegrationVO rtn = null;

    // Create the IntegrationVO used as criteria.
    String datasetSchemaId = datasetSchema.getIdDataSetSchema().toString();
    String dataflowId = datasetSchema.getIdDataFlow().toString();
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.DATASET_SCHEMA_ID, datasetSchemaId);
    internalParameters.put(IntegrationParams.DATAFLOW_ID, dataflowId);
    IntegrationVO criteria = new IntegrationVO();
    criteria.setInternalParameters(internalParameters);

    // Find all integrations matching the criteria.
    for (IntegrationVO integrationVO : integrationController
        .findAllIntegrationsByCriteria(criteria)) {
      if (IntegrationOperationTypeEnum.IMPORT.equals(integrationVO.getOperation())
          && mimeType.equalsIgnoreCase(
              integrationVO.getInternalParameters().get(IntegrationParams.FILE_EXTENSION))) {
        rtn = integrationVO;
        break;
      }
    }

    return rtn;
  }

  /**
   * Wipe data.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param delete the delete
   */
  private void wipeData(Long datasetId, String tableSchemaId, boolean delete) {
    if (delete) {
      if (null != tableSchemaId) {
        datasetService.deleteTableBySchema(tableSchemaId, datasetId);
      } else {
        datasetService.deleteImportData(datasetId);
      }
    }
  }

  /**
   * Gets the list of records.
   *
   * @param allRecords the all records
   * @return the list of records
   */
  private List<List<RecordValue>> getListOfRecords(List<RecordValue> allRecords) {
    List<List<RecordValue>> generalList = new ArrayList<>();

    // dividing the number of records in different lists
    int nLists = (int) Math.ceil(allRecords.size() / (double) batchSize);
    if (nLists > 1) {
      for (int i = 0; i < (nLists - 1); i++) {
        generalList.add(new ArrayList<>(allRecords.subList(batchSize * i, batchSize * (i + 1))));
      }
    }
    generalList
        .add(new ArrayList<>(allRecords.subList(batchSize * (nLists - 1), allRecords.size())));

    return generalList;
  }

  /**
   * Fill table id.
   *
   * @param idTableSchema the id table schema
   * @param listTableValues the list table values
   * @param oldTableId the old table id
   */
  private void fillTableId(final String idTableSchema, final List<TableValue> listTableValues,
      Long oldTableId) {
    if (oldTableId != null) {
      listTableValues.stream()
          .filter(tableValue -> tableValue.getIdTableSchema().equals(idTableSchema))
          .forEach(tableValue -> tableValue.setId(oldTableId));
    }
  }
}
