package org.eea.dataset.service.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.model.ImportSchemas;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The Class ZipUtils.
 */
@Component
public class ZipUtils {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The integration controller.
   */
  @Autowired
  private IntegrationControllerZuul integrationController;


  /**
   * The rules repository.
   */
  @Autowired
  private RulesRepository rulesRepository;

  /**
   * The unique constraint repository.
   */
  @Autowired
  private UniqueConstraintRepository uniqueConstraintRepository;

  /**
   * The rules controller zuul.
   */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;


  /**
   * Un zip import schema.
   *
   * @param is the is
   * @param fileName the file name
   * @return the import schemas
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ImportSchemas unZipImportSchema(InputStream is, String fileName)
      throws EEAException, IOException {

    ImportSchemas fileUnziped = new ImportSchemas();
    String multipartFileMimeType = getMimetype(fileName);

    List<DataSetSchema> schemas = new ArrayList<>();
    Map<String, String> schemaNames = new HashMap<>();
    Map<String, Long> schemaIds = new HashMap<>();
    List<IntegrationVO> extIntegrations = new ArrayList<>();
    List<UniqueConstraintSchema> uniques = new ArrayList<>();
    List<IntegrityVO> integrities = new ArrayList<>();
    List<byte[]> qcrulesBytes = new ArrayList<>();

    if ("zip".equalsIgnoreCase(multipartFileMimeType)) {
      try (ZipInputStream zip = new ZipInputStream(is)) {
        for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {

          String entryName = entry.getName();
          String mimeType = getMimetype(entryName);
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
            case "ids":
              schemaIds = unzippingDatasetIdsClasses(zip, schemaIds);
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
        fileUnziped.setSchemaIds(schemaIds);
      } finally {
        is.close();
      }
    }
    LOG.info("Schemas recovered from the Zip file {} during the import process", fileName);
    return fileUnziped;
  }


  /**
   * Zip schema.
   *
   * @param designs the designs
   * @param schemas the schemas
   * @param dataflowId the dataflow id
   *
   * @return the byte[]
   */
  public byte[] zipSchema(List<DesignDataset> designs, List<DataSetSchema> schemas,
      Long dataflowId) {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Map<String, String> schemaNames = new HashMap<>();
    Map<String, Long> schemaDatasetsId = new HashMap<>();
    try (ZipOutputStream zos = new ZipOutputStream(bos)) {
      for (DataSetSchema schema : schemas) {

        // Put the datasetSchemaId and the dataset name into a map to store later in the zip file
        DesignDataset design = designs.stream()
            .filter(d -> d.getDatasetSchema().equals(schema.getIdDataSetSchema().toString()))
            .findFirst().orElse(new DesignDataset());
        schemaNames.put(schema.getIdDataSetSchema().toString(), design.getDataSetName());
        schemaDatasetsId.put(schema.getIdDataSetSchema().toString(), design.getId());

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

      // Store the dataset ids
      zipDatasetIds(schemaDatasetsId, zos);


    } catch (Exception e) {
      LOG_ERROR.error("Error exporting schemas from the dataflowId {} to a ZIP file. Message {}",
          dataflowId, e.getMessage(), e);
    }
    return bos.toByteArray();

  }


  /**
   * Zip array list field schemas.
   *
   * @param listBytes the list bytes
   * @param datasetId the dataset id
   * @param tableNames the table names
   * @return the byte[]
   */
  public byte[] zipArrayListFieldSchemas(List<byte[]> listBytes, Long datasetId,
      List<String> tableNames) {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(bos)) {
      int i = 0;
      for (byte[] tableSchema : listBytes) {
        String nameFile = tableNames.get(i) + ".csv";
        InputStream is = new ByteArrayInputStream(tableSchema);
        zippingClasses(zos, nameFile, is);
        i++;
      }

    } catch (Exception e) {
      LOG_ERROR.error("Error exporting fieldschemas from the dataset {} to a ZIP file. Message {}",
          datasetId, e.getMessage(), e);
    }
    return bos.toByteArray();
  }


  private String getMimetype(final String file) throws EEAException {
    String mimeType = null;
    final int location = file.lastIndexOf('.');
    if (location == -1) {
      throw new EEAException(EEAErrorMessage.FILE_EXTENSION);
    }
    mimeType = file.substring(location + 1);
    return mimeType;
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
      zippingClasses(zos, nameFile, schemaStream);
    } catch (IOException e) {
      LOG_ERROR.error(
          "Error exporting the schema of the dataflow {} with datasetSchemaId {} into the zip. {}",
          schema.getIdDataFlow(), schema.getIdDataSetSchema(), e.getMessage(), e);
    }
  }

  /**
   * Zipping classes.
   *
   * @param zos the zos
   * @param fileName the file name
   * @param stream the stream
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void zippingClasses(ZipOutputStream zos, String fileName, InputStream stream)
      throws IOException {
    ZipEntry zeSchem = new ZipEntry(fileName);
    zos.putNextEntry(zeSchem);

    IOUtils.copyLarge(stream, zos);

    stream.close();
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
      zippingClasses(zos, nameFileRules, rulesStream);

    } catch (IOException e) {
      LOG_ERROR.error(
          "Error exporting the qcRules of the dataflow {} with datasetSchemaId {} into the zip. {}",
          schema.getIdDataFlow(), schema.getIdDataSetSchema(), e.getMessage(), e);
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
      zippingClasses(zos, nameFileUnique, uniqueStream);
    } catch (IOException e) {
      LOG_ERROR.error(
          "Error exporting the unique rules of the dataflow {} with datasetSchemaId {} into the zip. {}",
          schema.getIdDataFlow(), schema.getIdDataSetSchema(), e.getMessage(), e);
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
      zippingClasses(zos, nameFileIntegrity, integrityStream);

    } catch (IOException e) {
      LOG_ERROR.error(
          "Error exporting the integrity rules of the dataflow {} with datasetSchemaId {} into the zip. {}",
          schema.getIdDataFlow(), schema.getIdDataSetSchema(), e.getMessage(), e);
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
          integrationController.findAllIntegrationsByCriteria(integration);
      ObjectMapper objectMapperIntegration = new ObjectMapper();
      InputStream extIntegrationStream =
          new ByteArrayInputStream(objectMapperIntegration.writeValueAsBytes(extIntegrations));
      String nameFileExtIntegrations = fileName + ".extintegrations";
      zippingClasses(zos, nameFileExtIntegrations, extIntegrationStream);

    } catch (IOException e) {
      LOG_ERROR.error(
          "Error exporting the external integrations of the dataflow {} with datasetSchemaId {} into the zip. {}",
          dataflowId, schema.getIdDataSetSchema(), e.getMessage(), e);
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
      zippingClasses(zos, "datasetSchemaNames.names", schemaNamesStream);
    } catch (IOException e) {
      LOG_ERROR.error("Error exporting the dataset names into the zip. {}", e.getMessage(), e);
    }
  }

  /**
   * Zip dataset ids.
   *
   * @param schemaDatasetsId the schema datasets id
   * @param zos the zos
   */
  private void zipDatasetIds(Map<String, Long> schemaDatasetsId, ZipOutputStream zos) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      InputStream schemaDatasetsIdsStream =
          new ByteArrayInputStream(objectMapper.writeValueAsBytes(schemaDatasetsId));
      zippingClasses(zos, "datasetSchemaIds.ids", schemaDatasetsIdsStream);
    } catch (IOException e) {
      LOG_ERROR.error("Error exporting the dataset ids into the zip. {}", e.getMessage(), e);
    }
  }


  /**
   * Unzipping schema classes.
   *
   * @param zip the zip
   * @param schemas the schemas
   *
   * @return the list
   */
  private List<DataSetSchema> unzippingSchemaClasses(ZipInputStream zip,
      List<DataSetSchema> schemas) {

    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DataSetSchema schema = objectMapper.readValue(content, DataSetSchema.class);
        schemas.add(schema);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error unzipping the schemas classes during the import process. Message {}",
          e.getMessage(), e);
    }
    return schemas;
  }


  /**
   * Unzipping qc classes.
   *
   * @param zip the zip
   * @param qcrulesBytes the qcrules bytes
   *
   * @return the list
   */
  private List<byte[]> unzippingQcClasses(ZipInputStream zip, List<byte[]> qcrulesBytes) {
    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        qcrulesBytes.add(content);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error unzipping the qcrules during the import process. Message {}",
          e.getMessage(), e);
    }
    return qcrulesBytes;
  }

  /**
   * Unzipping unique classes.
   *
   * @param zip the zip
   * @param uniques the uniques
   *
   * @return the list
   */
  private List<UniqueConstraintSchema> unzippingUniqueClasses(ZipInputStream zip,
      List<UniqueConstraintSchema> uniques) {
    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        uniques
            .addAll(Arrays.asList(objectMapper.readValue(content, UniqueConstraintSchema[].class)));
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error unzipping the unique rules during the import process. Message {}",
          e.getMessage(), e);
    }
    return uniques;
  }


  /**
   * Unzipping dataset names classes.
   *
   * @param zip the zip
   * @param schemaNames the schema names
   *
   * @return the map
   */
  private Map<String, String> unzippingDatasetNamesClasses(ZipInputStream zip,
      Map<String, String> schemaNames) {
    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        schemaNames = objectMapper.readValue(content, Map.class);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error unzipping the dataset names during the import process. Message {}",
          e.getMessage(), e);
    }
    return schemaNames;
  }


  /**
   * Unzipping dataset ids classes.
   *
   * @param zip the zip
   * @param schemaIds the schema ids
   *
   * @return the map
   */
  private Map<String, Long> unzippingDatasetIdsClasses(ZipInputStream zip,
      Map<String, Long> schemaIds) {
    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        schemaIds = objectMapper.readValue(content, Map.class);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error unzipping the dataset ids during the import process. Message {}",
          e.getMessage(), e);
    }
    return schemaIds;
  }


  /**
   * Unzipping ext integrations classes.
   *
   * @param zip the zip
   * @param extIntegrations the ext integrations
   *
   * @return the list
   */
  private List<IntegrationVO> unzippingExtIntegrationsClasses(ZipInputStream zip,
      List<IntegrationVO> extIntegrations) {
    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        extIntegrations
            .addAll(Arrays.asList(objectMapper.readValue(content, IntegrationVO[].class)));
      }
    } catch (Exception e) {
      LOG_ERROR.error(
          "Error unzipping the external integrations during the import process. Message {}",
          e.getMessage(), e);
    }
    return extIntegrations;
  }

  /**
   * Unzipping integrity qc classes.
   *
   * @param zip the zip
   * @param integrities the integrities
   *
   * @return the list
   */
  private List<IntegrityVO> unzippingIntegrityQcClasses(ZipInputStream zip,
      List<IntegrityVO> integrities) {
    try {
      byte[] content = IOUtils.toByteArray(zip);
      if (content != null && content.length > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        integrities.addAll(Arrays.asList(objectMapper.readValue(content, IntegrityVO[].class)));
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error unzipping the integration rules during the import process. Message {}",
          e.getMessage(), e);
    }
    return integrities;
  }


}
