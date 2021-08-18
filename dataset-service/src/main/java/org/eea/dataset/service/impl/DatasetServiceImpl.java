package org.eea.dataset.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.transaction.Transactional;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.persistence.data.SortFieldsHelper;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.AttachmentRepository;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository.IDError;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.sequence.FieldValueIdGenerator;
import org.eea.dataset.persistence.data.sequence.RecordValueIdGenerator;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.persistence.metabase.repository.TestDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.PaMService;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.helper.PostgresBulkImporter;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

/**
 * The Class DatasetServiceImpl.
 */
@Service("datasetService")
public class DatasetServiceImpl implements DatasetService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant USER: {@value}. */
  private static final String USER = "root";

  /** The Constant HEADER_NAME: {@value}. */
  private static final String HEADER_NAME = "headerName";

  /** The Constant DATASET_ID: {@value}. */
  private static final String DATASET_ID = "dataset_%s";

  /** The Constant FILE_PUBLIC_DATASET_PATTERN_NAME. */
  private static final String FILE_PUBLIC_DATASET_PATTERN_NAME = "%s-%s";



  /** The field max length. */
  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

  /** The path public file. */
  @Value("${pathPublicFile}")
  private String pathPublicFile;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The partition data set metabase repository. */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The dataflow controller zull. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The record validation repository. */
  @Autowired
  private RecordValidationRepository recordValidationRepository;

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /** The field validation repository. */
  @Autowired
  private FieldValidationRepository fieldValidationRepository;

  /** The statistics repository. */
  @Autowired
  private StatisticsRepository statisticsRepository;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The record mapper. */
  @Autowired
  private RecordMapper recordMapper;

  /** The record value id generator. */
  @Autowired
  private RecordValueIdGenerator recordValueIdGenerator;

  /** The field value id generator. */
  @Autowired
  private FieldValueIdGenerator fieldValueIdGenerator;

  /** The file export factory. */
  @Autowired
  private IFileExportFactory fileExportFactory;

  /** The record no validation mapper. */
  @Autowired
  private RecordNoValidationMapper recordNoValidationMapper;

  /** The field validation mapper. */
  @Autowired
  private FieldValidationMapper fieldValidationMapper;

  /** The record validation mapper. */
  @Autowired
  private RecordValidationMapper recordValidationMapper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The field no validation mapper. */
  @Autowired
  private FieldNoValidationMapper fieldNoValidationMapper;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /** The pa M service. */
  @Autowired
  private PaMService paMService;

  /** The attachment repository. */
  @Autowired
  private AttachmentRepository attachmentRepository;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The pk catalogue repository. */
  @Autowired
  private PkCatalogueRepository pkCatalogueRepository;

  /** The Test dataset repository. */
  @Autowired
  private TestDatasetRepository testDatasetRepository;


  /** The reference dataset repository. */
  @Autowired
  private ReferenceDatasetRepository referenceDatasetRepository;

  /** The record store controller. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The import path. */
  @Value("${importPath}")
  private String importPath;

  /**
   * Save all records.
   *
   * @param datasetId the dataset id
   * @param recordValues the record values
   */
  @Override
  @Transactional
  public void saveAllRecords(Long datasetId, List<RecordValue> recordValues) {
    recordRepository.saveAll(recordValues);
  }

  /**
   * Save table.
   *
   * @param datasetId the dataset id
   * @param tableValue the table value
   */
  @Override
  @Transactional
  public void saveTable(@DatasetId Long datasetId, TableValue tableValue) {
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
    Optional<DatasetValue> datasetValue = datasetRepository.findById(datasetId);
    if (datasetValue.isPresent()) {
      tableValue.setDatasetId(datasetValue.get());
      tableRepository.saveAndFlush(tableValue);
    }
  }

  /**
   * Gets the mimetype.
   *
   * @param file the file
   *
   * @return the mimetype
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public String getMimetype(final String file) throws EEAException {
    String mimeType = null;
    final int location = file.lastIndexOf('.');
    if (location == -1) {
      throw new EEAException(EEAErrorMessage.FILE_EXTENSION);
    }
    mimeType = file.substring(location + 1);
    return mimeType;
  }

  /**
   * Delete data schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  @CacheEvict(value = "dataFlowId", key = "#datasetId")
  public void deleteDataSchema(final String datasetId) {
    schemasRepository.deleteById(new ObjectId(datasetId));
  }

  /**
   * Delete table by schema.
   *
   * @param tableSchemaId the id table schema
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteTableBySchema(final String tableSchemaId, final Long datasetId) {
    deleteRecords(datasetId, tableSchemaId);
  }

  /**
   * Delete records.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  private void deleteRecords(Long datasetId, String tableSchemaId) {

    boolean singleTable = null != tableSchemaId;
    Long dataflowId = getDataFlowIdById(datasetId);
    TypeStatusEnum dataflowStatus = dataflowControllerZuul.getMetabaseById(dataflowId).getStatus();
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(
        new ObjectId(datasetMetabaseService.findDatasetSchemaIdById(datasetId)));
    DatasetTypeEnum datasetType = getDatasetType(datasetId);
    Boolean referenceUpdateable = Boolean.TRUE.equals(referenceDatasetRepository.findById(datasetId)
        .orElse(new ReferenceDataset()).getUpdatable());

    for (TableSchema tableSchema : schema.getTableSchemas()) {

      String loopTableSchemaId = tableSchema.getIdTableSchema().toString();

      if (singleTable && !tableSchemaId.equals(loopTableSchemaId)) {
        continue;
      }

      if (TypeStatusEnum.DESIGN.equals(dataflowStatus)) {
        deleteRecordsFromIdTableSchema(datasetId, loopTableSchemaId);
      } else if (Boolean.TRUE.equals(tableSchema.getReadOnly())
          && !(DatasetTypeEnum.REFERENCE.equals(datasetType)
              && Boolean.TRUE.equals(referenceUpdateable))) {
        LOG.info("Skipped deleteRecords: datasetId={}, tableSchemaId={}, tableSchema.readOnly={}",
            datasetId, loopTableSchemaId, tableSchema.getReadOnly());
      } else if (Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        List<String> fieldSchemasToClear = tableSchema.getRecordSchema().getFieldSchema().stream()
            .filter(fieldSchema -> Boolean.FALSE.equals(fieldSchema.getReadOnly()))
            .map(fieldSchema -> fieldSchema.getIdFieldSchema().toString())
            .collect(Collectors.toList());
        List<FieldValue> fieldValuesToClear =
            fieldRepository.findAllByIdFieldSchemaIn(fieldSchemasToClear);
        fieldValuesToClear.stream().forEach(fieldVal -> fieldVal.setValue(""));
        fieldRepository.saveAll(fieldValuesToClear);
        LOG.info("Overwritting fieldValues to blank: datasetId={}, tableSchemaId={}", datasetId,
            loopTableSchemaId);
      } else {
        deleteRecordsFromIdTableSchema(datasetId, loopTableSchemaId);
      }

      if (singleTable) {
        return;
      }
    }

    try {
      saveStatistics(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error saving statistics after deleting all the dataset values. Error message: {}",
          e.getMessage(), e);
    }
  }


  /**
   * Delete import data.
   *
   * @param datasetId the data set id
   */
  @Override
  @Transactional
  public void deleteImportData(final Long datasetId) {
    deleteRecords(datasetId, null);
  }

  /**
   * Gets the table values by id.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public TableVO getTableValuesById(final Long datasetId, final String idTableSchema,
      Pageable pageable, final String fields, ErrorTypeEnum[] levelError, String[] idRules,
      String fieldSchema, String fieldValue) throws EEAException {
    List<String> commonShortFields = new ArrayList<>();
    Map<String, Integer> mapFields = new HashMap<>();
    List<SortField> sortFieldsArray = new ArrayList<>();
    SortField[] newFields = null;
    TableVO result = new TableVO();
    Long totalRecords = tableRepository.countRecordsByIdTableSchema(idTableSchema);
    if (totalRecords == 0) {
      result.setTotalFilteredRecords(0L);
      result.setTableValidations(new ArrayList<>());
      result.setTotalRecords(0L);
      result.setRecords(new ArrayList<>());
    } else {
      // Check if we need to put all the records without pagination
      pageable = calculatePageable(pageable, totalRecords);

      result = calculatedErrorsAndRecordsToSee(datasetId, idTableSchema, pageable, fields,
          levelError, commonShortFields, mapFields, sortFieldsArray, newFields, result, idRules,
          fieldSchema, fieldValue);

      // Table with out values
      if (null == result.getRecords() || result.getRecords().isEmpty()) {
        result.setRecords(new ArrayList<>());
        LOG.info("No records found in datasetId {}, idTableSchema {}", datasetId, idTableSchema);

      } else {
        List<RecordVO> recordVOs = result.getRecords();

        LOG.info(
            "Total records found in datasetId {} idTableSchema {}: {}. Now in page {}, {} records by page",
            datasetId, idTableSchema, recordVOs.size(),
            pageable != null ? pageable.getPageNumber() : null,
            pageable != null ? pageable.getPageSize() : null);
        if (null != fields) {
          LOG.info("Ordered by idFieldSchema {}", commonShortFields);
        }

        // Retrieve validations to set them into the final result
        retrieveValidations(recordVOs);
      }
    }
    result.setTotalRecords(totalRecords);
    return result;
  }

  /**
   * Gets the by id.
   *
   * @param datasetId the dataset id
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  @Deprecated
  public DataSetVO getById(Long datasetId) throws EEAException {
    DatasetValue datasetValue = new DatasetValue();
    List<TableValue> allTableValues = tableRepository.findAllTables();
    datasetValue.setTableValues(allTableValues);
    datasetValue.setId(datasetId);
    datasetValue.setIdDatasetSchema(datasetRepository.findIdDatasetSchemaById(datasetId));
    for (TableValue tableValue : allTableValues) {
      tableValue
          .setRecords(sanitizeRecords(retrieveRecordValue(tableValue.getIdTableSchema(), null)));
    }
    LOG.info("Get dataset by id: {}", datasetId);
    return dataSetMapper.entityToClass(datasetValue);
  }

  /**
   * Update dataset.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateDataset(Long datasetId, DataSetVO dataset) throws EEAException {
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    DatasetValue datasetValue = dataSetMapper.classToEntity(dataset);
    datasetRepository.saveAndFlush(datasetValue);
  }

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   *
   * @return the data flow id by id
   */
  @Override
  @Cacheable(value = "dataFlowId", key = "#datasetId")
  public Long getDataFlowIdById(Long datasetId) {
    return dataSetMetabaseRepository.findDataflowIdById(datasetId);
  }

  /**
   * Gets the position from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param type the type
   *
   * @return the position from any object id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public ValidationLinkVO getPositionFromAnyObjectId(final String id, final Long idDataset,
      final EntityTypeEnum type) throws EEAException {

    ValidationLinkVO validationLink = new ValidationLinkVO();
    RecordValue record = new RecordValue();
    List<RecordValue> records = new ArrayList<>();

    // TABLE
    if (EntityTypeEnum.TABLE == type) {
      TableValue table = tableRepository.findByIdAndDatasetId_Id(Long.parseLong(id), idDataset);
      records = recordRepository.findByTableValueNoOrder(table.getIdTableSchema(), null);
      if (records != null && !records.isEmpty()) {
        record = records.get(0);
      }
    }

    // RECORD
    if (EntityTypeEnum.RECORD == type) {
      record = recordRepository.findByIdAndTableValue_DatasetId_Id(id, idDataset);
      records =
          recordRepository.findByTableValueNoOrder(record.getTableValue().getIdTableSchema(), null);
    }

    // FIELD
    if (EntityTypeEnum.FIELD == type) {
      FieldValue field = fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(id, idDataset);
      if (field != null && field.getRecord() != null && field.getRecord().getTableValue() != null) {
        record = field.getRecord();
        records = recordRepository
            .findByTableValueNoOrder(record.getTableValue().getIdTableSchema(), null);
      }
    }

    if (records != null && !records.isEmpty()) {
      int recordPosition = records.indexOf(record);
      validationLink.setIdTableSchema(record.getTableValue().getIdTableSchema());
      validationLink.setPosition(Long.valueOf(recordPosition));
      validationLink.setIdRecord(record.getId());
    }

    LOG.info(
        "Validation error with idObject {} clicked in dataset {}. The position is {} from table schema {}",
        id, idDataset, validationLink.getPosition(), validationLink.getIdTableSchema());
    return validationLink;
  }

  /**
   * Save statistics.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void saveStatistics(final Long datasetId) throws EEAException {
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());

    if (dataset.getId() != null && StringUtils.isNotBlank(dataset.getIdDatasetSchema())) {
      List<Statistics> statsList = Collections.synchronizedList(new ArrayList<>());
      List<TableValue> allTableValues = dataset.getTableValues();

      DataSetMetabase datasetMb =
          dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());

      DataSetSchema schema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getIdDatasetSchema()));

      Map<String, String> mapIdNameDatasetSchema = new HashMap<>();
      for (TableSchema tableSchema : schema.getTableSchemas()) {

        mapIdNameDatasetSchema.put(tableSchema.getIdTableSchema().toString(),
            tableSchema.getNameTableSchema());
      }

      allTableValues.parallelStream().forEach(tableValue -> statsList
          .addAll(processTableStats(tableValue, datasetId, mapIdNameDatasetSchema)));

      // Check dataset validations
      Boolean datasetErrors = false;
      if (dataset.getDatasetValidations() != null && !dataset.getDatasetValidations().isEmpty()) {
        datasetErrors = true;
      } else {
        Optional<Statistics> opt = statsList.stream()
            .filter(s -> "tableErrors".equals(s.getStatName()) && "true".equals(s.getValue()))
            .findFirst();
        if (opt.isPresent()) {
          datasetErrors = true;
        }
      }

      statsList.add(fillStat(datasetId, null, "idDataSetSchema", dataset.getIdDatasetSchema()));
      statsList.add(fillStat(datasetId, null, "nameDataSetSchema", datasetMb.getDataSetName()));
      statsList.add(fillStat(datasetId, null, "datasetErrors", datasetErrors.toString()));


      statisticsRepository.deleteStatsByIdDataset(datasetId);
      statisticsRepository.flush();
      statisticsRepository.saveAll(statsList);
      LOG.info("Statistics saved to datasetId {}.", datasetId);
    } else {
      LOG_ERROR.error("No dataset found to save statistics. DatasetId:{}", datasetId);
    }
  }

  /**
   * Update records. if we fill updateCascadePk it update the pk in cascade in the dataset
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateRecords(final Long datasetId, final List<RecordVO> records,
      boolean updateCascadePK) throws EEAException {
    if (datasetId == null || records == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);

    }
    List<RecordValue> recordValues = recordMapper.classListToEntity(records);
    List<FieldValue> fieldValues = new ArrayList<>();


    if (updateCascadePK) {
      // we update the ids in cascade for any pk value changed
      for (RecordValue recordValue : recordValues) {
        updateCascadePK(recordValue.getId(), recordValue.getFields());
      }
    }
    String datasetSchemaId = dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
    DatasetTypeEnum datasetType = getDatasetType(datasetId);
    for (RecordValue recordValue : recordValues) {
      fieldValueUpdateRecordFor(fieldValues, datasetSchemaId, recordValue, datasetType);
    }
    fieldRepository.saveAll(fieldValues);

  }


  /**
   * Field value update record for.
   *
   * @param fieldValues the field values
   * @param datasetSchemaId the dataset schema id
   * @param recordValue the record value
   * @param datasetType the dataset type
   */
  private void fieldValueUpdateRecordFor(List<FieldValue> fieldValues, String datasetSchemaId,
      RecordValue recordValue, DatasetTypeEnum datasetType) {
    for (FieldValue fieldValue : recordValue.getFields()) {
      if (null == fieldValue.getValue()) {
        fieldValue.setValue("");
      } else {
        fieldValue.setValue(fieldValue.getValue());
        if (null == fieldValue.getType()) {
          if (null != fieldValue.getValue() && fieldValue.getValue().length() >= fieldMaxLength) {
            fieldValue.setValue(fieldValue.getValue().substring(0, fieldMaxLength));
          }
        } else {
          switch (fieldValue.getType()) {
            case POINT:
              break;
            case LINESTRING:
              break;
            case POLYGON:
              break;
            case MULTIPOINT:
              break;
            case MULTILINESTRING:
              break;
            case MULTIPOLYGON:
              break;
            case GEOMETRYCOLLECTION:
              break;
            default:
              if (fieldValue.getValue().length() >= fieldMaxLength) {
                fieldValue.setValue(fieldValue.getValue().substring(0, fieldMaxLength));
              }
          }
        }
      }
      Document fieldSchema =
          schemasRepository.findFieldSchema(datasetSchemaId, fieldValue.getIdFieldSchema());
      if (!(fieldSchema != null && fieldSchema.get(LiteralConstants.READ_ONLY) != null
          && fieldSchema.getBoolean(LiteralConstants.READ_ONLY)
          && !DatasetTypeEnum.DESIGN.equals(datasetType))) {
        fieldValues.add(fieldValue);
      }
    }
  }

  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param recordVOs the record V os
   * @param tableSchemaId the table schema id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertRecords(Long datasetId, List<RecordVO> recordVOs, String tableSchemaId)
      throws EEAException {

    if (recordVOs == null || recordVOs.isEmpty()) {
      throw new EEAException(EEAErrorMessage.RECORD_REQUIRED);
    }

    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
    TableSchema tableSchema = getTableSchema(tableSchemaId, datasetMetabaseVO.getDatasetSchema());

    if (null == tableSchema) {
      throw new EEAException(EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }

    DatasetTypeEnum datasetType = getDatasetType(datasetId);
    String dataProviderCode = null != datasetMetabaseVO.getDataProviderId()
        ? representativeControllerZuul.findDataProviderById(datasetMetabaseVO.getDataProviderId())
            .getCode()
        : null;

    if (!DatasetTypeEnum.DESIGN.equals(datasetType)
        && !DatasetTypeEnum.REFERENCE.equals(datasetType)) {

      // Deny insert if the table is marked as read only. Not applies for DESIGN.
      if (Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        throw new EEAException(EEAErrorMessage.TABLE_READ_ONLY);
      }

      // Deny insert if the table is marked as fixed number of record. Not applies for DESIGN.
      if (Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        throw new EEAException(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS);
      }
    }

    recordRepository
        .saveAll(createRecords(datasetId, dataProviderCode, recordVOs, datasetType, tableSchema));
  }

  /**
   * Delete record. if we fill deleteCascadePk it delete the pk in cascade in the dataset
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @param deleteCascadePK the delete cascade
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteRecord(final Long datasetId, final String recordId,
      final boolean deleteCascadePK) throws EEAException {
    if (datasetId == null || recordId == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (deleteCascadePK) {
      deleteCascadePK(recordId);
    }
    recordRepository.deleteRecordWithId(recordId);
  }

  /**
   * Delete cascade PK.
   *
   * @param recordId the record id
   */
  private void deleteCascadePK(final String recordId) {
    RecordValue record = recordRepository.findById(recordId);
    Map<String, FieldValue> mapField = new HashMap<>();
    // get fields in record
    record.getFields().stream().forEach(field -> mapField.put(field.getIdFieldSchema(), field));
    // get the RecordSchema
    Document recordSchemaDocument = schemasRepository.findRecordSchema(
        record.getTableValue().getDatasetId().getIdDatasetSchema(),
        record.getTableValue().getIdTableSchema());
    // get fks to delete
    if (null != recordSchemaDocument) {
      List<Document> fieldSchemasList =
          (ArrayList) recordSchemaDocument.get(LiteralConstants.FIELD_SCHEMAS);

      List<String> recordsToDelete = findRecordsToDelete(mapField, fieldSchemasList);
      if (!recordsToDelete.isEmpty()) {
        LOG.info("records with fk's to delete {}", recordsToDelete);
        recordRepository.deleteRecordWithIdIn(recordsToDelete);
      }
    }
    // delete all fks
  }

  /**
   * Find records to delete.
   *
   * @param mapField the map field
   * @param fieldSchemasList the field schemas list
   *
   * @return the list
   */
  private List<String> findRecordsToDelete(Map<String, FieldValue> mapField,
      List<Document> fieldSchemasList) {
    List<String> recordsToDelete = new ArrayList<>();
    for (Object document : fieldSchemasList) {
      Document fieldSchemaDocument = (Document) document;
      if (fieldSchemaDocument.get(LiteralConstants.PK) != null
          && fieldSchemaDocument.getBoolean(LiteralConstants.PK)) {
        String idFieldSchema = (fieldSchemaDocument.get(LiteralConstants.ID)).toString();
        PkCatalogueSchema pkCatalogueSchema =
            pkCatalogueRepository.findByIdPk(new ObjectId(idFieldSchema));
        if (null != pkCatalogueSchema && pkCatalogueSchema.getReferenced() != null) {
          List<String> referenced = pkCatalogueSchema.getReferenced().stream()
              .map(ObjectId::toString).collect(Collectors.toList());
          List<FieldValue> fieldsValues = fieldRepository.findByIdFieldSchemaIn(referenced);

          FieldValue fieldV = mapField.get(idFieldSchema);
          fieldsValues.stream().forEach(field -> {
            if (fieldV != null && field.getValue().equals(fieldV.getValue())) {
              recordsToDelete.add(field.getRecord().getId());
            }
          });
          // delete pams list
          paMService.deleteGroups(fieldSchemasList, fieldV.getValue());
        }
      }
    }
    return recordsToDelete;
  }

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param tableSchemaId the table schema id
   *
   * @return the byte[]
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public byte[] exportFile(Long datasetId, String mimeType, final String tableSchemaId)
      throws EEAException, IOException {
    // Get the dataFlowId from the metabase
    Long idDataflow = getDataFlowIdById(datasetId);

    // Find if the dataset type is EU to include the countryCode
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
    boolean includeCountryCode = DatasetTypeEnum.EUDATASET.equals(datasetType)
        || DatasetTypeEnum.COLLECTION.equals(datasetType);

    final IFileExportContext contextExport = fileExportFactory.createContext(mimeType);
    LOG.info("End of exportFile");
    return contextExport.fileWriter(idDataflow, datasetId, tableSchemaId, includeCountryCode,
        false);
  }



  /**
   * Export file through integration.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void exportFileThroughIntegration(Long datasetId, Long integrationId) throws EEAException {
    DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(datasetId)
        .orElseThrow(() -> new EEAException(EEAErrorMessage.DATASET_NOTFOUND));
    String datasetSchemaId = datasetMetabase.getDatasetSchema();
    IntegrationVO integrationVO =
        integrationController.findExportIntegration(datasetSchemaId, integrationId);
    integrationController.executeIntegrationProcess(IntegrationToolTypeEnum.FME,
        IntegrationOperationTypeEnum.EXPORT, null, datasetId, integrationVO);
  }

  /**
   * Insert schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertSchema(final Long datasetId, String idDatasetSchema) throws EEAException {

    // 1.Insert the dataset schema into DatasetValue
    DatasetValue ds = new DatasetValue();
    ds.setIdDatasetSchema(idDatasetSchema);
    ds.setId(datasetId);

    // 2.Search the table schemas of the dataset and then insert it into TableValue
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));
    List<TableValue> tableValues = new ArrayList<>();
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      TableValue tv = new TableValue();
      tv.setIdTableSchema(tableSchema.getIdTableSchema().toString());
      tv.setDatasetId(ds);
      tableValues.add(tv);
    }
    ds.setTableValues(tableValues);
    datasetRepository.save(ds);
  }

  /**
   * Update field. if cascade pk is true we update the pk in cascade
   *
   * @param datasetId the dataset id
   * @param field the field
   * @param updateCascadePK the update cascade PK
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateField(final Long datasetId, final FieldVO field, boolean updateCascadePK)
      throws EEAException {
    if (datasetId == null || field == null) {
      throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);

    }

    String datasetSchemaId = dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
    Document fieldSchema =
        schemasRepository.findFieldSchema(datasetSchemaId, field.getIdFieldSchema());

    if (fieldSchema == null) {
      throw new EEAException(EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    }

    Boolean isLinkMultiselect = Boolean.FALSE;
    if (DataType.LINK.equals(field.getType()) || DataType.EXTERNAL_LINK.equals(field.getType())) {
      isLinkMultiselect = fieldSchema.get(LiteralConstants.PK_HAS_MULTIPLE_VALUES) != null
          && fieldSchema.getBoolean(LiteralConstants.PK_HAS_MULTIPLE_VALUES);
    }
    if (fieldSchema.get(LiteralConstants.READ_ONLY) != null
        && (Boolean) fieldSchema.get(LiteralConstants.READ_ONLY)
        && !DatasetTypeEnum.DESIGN.equals(getDatasetType(datasetId))) {
      throw new EEAException(EEAErrorMessage.FIELD_READ_ONLY);
    }
    // if the type is multiselect codelist or Link multiselect we sort the values in lexicographic
    // order
    if ((DataType.MULTISELECT_CODELIST.equals(field.getType()) || isLinkMultiselect)
        && null != field.getValue()) {
      List<String> values = new ArrayList<>();
      Arrays.asList(field.getValue().split(";")).stream()
          .forEach(value -> values.add(value.trim()));
      Collections.sort(values);
      String codelist = "";
      for (int i = 0; i < values.size(); i++) {
        if (i == 0) {
          codelist = values.get(0);
        } else {
          codelist = codelist + "; " + values.get(i);
        }
      }
      field.setValue(codelist);
    }
    if (updateCascadePK) {
      fieldValueUpdatePK(field, fieldSchema, datasetSchemaId);
    }
    field.setValue(field.getValue());
    if (null == field.getType()) {
      if (null != field.getValue() && field.getValue().length() >= fieldMaxLength) {
        field.setValue(field.getValue().substring(0, fieldMaxLength));
      }
    } else {
      switch (field.getType()) {
        case POINT:
          break;
        case LINESTRING:
          break;
        case POLYGON:
          break;
        case MULTIPOINT:
          break;
        case MULTILINESTRING:
          break;
        case MULTIPOLYGON:
          break;
        case GEOMETRYCOLLECTION:
          break;
        default:
          if (null != field.getValue() && field.getValue().length() >= fieldMaxLength) {
            field.setValue(field.getValue().substring(0, fieldMaxLength));
          }
      }
    }

    fieldRepository.saveValue(field.getId(), field.getValue());
  }


  /**
   * Find table id by table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   *
   * @return the long
   */
  @Override
  public Long findTableIdByTableSchema(Long datasetId, String idTableSchema) {
    return tableRepository.findIdByIdTableSchema(idTableSchema);
  }

  /**
   * Delete record values to restore snapshot.
   *
   * @param datasetId the dataset id
   * @param idPartition the id partition
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @org.springframework.transaction.annotation.Transactional(propagation = Propagation.NESTED)
  public void deleteRecordValuesToRestoreSnapshot(Long datasetId, Long idPartition)
      throws EEAException {
    recordRepository.deleteRecordValuesToRestoreSnapshot(idPartition);
  }

  /**
   * Save table propagation.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void saveTablePropagation(Long datasetId, TableSchemaVO tableSchema) throws EEAException {
    TableValue table = new TableValue();
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
    Optional<DatasetValue> dataset = datasetRepository.findById(datasetId);
    if (dataset.isPresent()) {
      table.setIdTableSchema(tableSchema.getIdTableSchema());
      table.setDatasetId(dataset.get());
      saveTable(datasetId, table);
    } else {
      LOG_ERROR.error("Saving table propagation failed because the dataset {} is not found",
          datasetId);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
  }

  /**
   * Delete table value.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  @Override
  @Transactional
  public void deleteTableValue(Long datasetId, String idTableSchema) {
    tableRepository.deleteByIdTableSchema(idTableSchema);
  }

  /**
   * Delete field values.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  @Override
  @Transactional
  public void deleteFieldValues(Long datasetId, String fieldSchemaId) {
    LOG.info(
        "Deleting the related field values from the datasetId {} due to deleting the fieldSchema {}",
        datasetId, fieldSchemaId);
    fieldRepository.deleteByIdFieldSchemaNative(fieldSchemaId);
  }

  /**
   * Update field value type.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   * @param type the type
   */
  @Override
  @Transactional
  public void updateFieldValueType(Long datasetId, String fieldSchemaId, DataType type) {
    fieldRepository.updateFieldValueType(fieldSchemaId, type.getValue());
  }

  /**
   * Delete all table values.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteAllTableValues(Long datasetId) {
    tableRepository.removeTableData(datasetId);
  }

  /**
   * Prepare new field propagation.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void prepareNewFieldPropagation(Long datasetId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {

    // Find, from the idFieldSchema, the idTableSchema to know the table and the records containing
    // it to propagate
    Integer sizeRecords = 0;
    Optional<DatasetValue> dataset = datasetRepository.findById(datasetId);
    if (dataset.isPresent()) {
      String idDatasetSchema = dataset.get().getIdDatasetSchema();
      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));

      Optional<TableSchema> tableSchema = schema.getTableSchemas().stream()
          .filter(t -> t.getRecordSchema().getFieldSchema().stream()
              .anyMatch(f -> f.getIdFieldSchema().equals(new ObjectId(fieldSchemaVO.getId()))))
          .findFirst();

      if (tableSchema.isPresent()) {
        String idTableSchema = tableSchema.get().getIdTableSchema().toString();

        Optional<TableValue> table = dataset.get().getTableValues().stream()
            .filter(t -> t.getIdTableSchema().equals(idTableSchema)).findFirst();

        if (table.isPresent()) {

          sizeRecords = table.get().getRecords().size();

          Map<String, Object> value = new HashMap<>();
          value.put("dataset_id", datasetId);
          value.put("sizeRecords", sizeRecords);
          value.put("idTableSchema", idTableSchema);
          value.put("idFieldSchema", fieldSchemaVO.getId());
          value.put("typeField", fieldSchemaVO.getType());
          kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_NEW_DESIGN_FIELD_PROPAGATION, value);

        }
      }
    } else {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
  }

  /**
   * Save new field propagation.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param idFieldSchema the id field schema
   * @param typeField the type field
   */
  @Override
  @Transactional
  public void saveNewFieldPropagation(Long datasetId, String idTableSchema, Pageable pageable,
      String idFieldSchema, DataType typeField) {

    List<RecordValue> recordsPaginated =
        recordRepository.findByTableValue_IdTableSchema(idTableSchema, pageable);

    List<FieldValue> fields = new ArrayList<>();
    for (RecordValue r : recordsPaginated) {
      FieldValue field = new FieldValue();
      field.setIdFieldSchema(idFieldSchema);
      field.setType(typeField);
      field.setValue("");
      RecordValue recordAux = new RecordValue();
      TableValue tableAux = new TableValue();
      DatasetValue datasetAux = new DatasetValue();
      datasetAux.setId(datasetId);
      tableAux.setDatasetId(datasetAux);
      recordAux.setTableValue(tableAux);
      recordAux.setId(r.getId());
      field.setRecord(recordAux);

      fields.add(field);
    }

    fieldRepository.saveAll(fields);
  }

  /**
   * Delete record values by provider.
   *
   * @param datasetId the dataset id
   * @param providerCode the provider code
   */
  @Override
  @Transactional
  public void deleteRecordValuesByProvider(@DatasetId Long datasetId, String providerCode) {
    LOG.info("Deleting data with providerCode: {} ", providerCode);
    recordRepository.deleteByDataProviderCode(providerCode);
  }

  /**
   * Gets the field values referenced.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param conditionalValue the conditional value
   * @param searchValue the search value
   * @param resultsNumber the results number
   * @return the field values referenced
   * @throws EEAException the EEA exception
   */
  @Override
  public List<FieldVO> getFieldValuesReferenced(Long datasetIdOrigin, String datasetSchemaId,
      String fieldSchemaId, String conditionalValue, String searchValue, Integer resultsNumber)
      throws EEAException {

    List<FieldVO> fieldsVO = new ArrayList<>();
    Document fieldSchema = schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaId);
    Document referenced = (Document) fieldSchema.get(LiteralConstants.REFERENCED_FIELD);
    if (referenced != null) {
      String idPk = referenced.get("idPk").toString();
      String labelSchemaId = null;
      String conditionalSchemaId = null;
      if (referenced.get("labelId") != null) {
        labelSchemaId = referenced.get("labelId").toString();
      } else {
        // In case there's no label selected, the label will the the same as the Pk
        labelSchemaId = idPk;
      }
      if (referenced.get("linkedConditionalFieldId") != null) {
        conditionalSchemaId = referenced.get("linkedConditionalFieldId").toString();
      }
      if (StringUtils.isBlank(searchValue)) {
        searchValue = "";
      }

      Long idDatasetDestination =
          datasetMetabaseService.getDatasetDestinationForeignRelation(datasetIdOrigin, idPk);

      TenantResolver
          .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDatasetDestination));
      // If the variable resultsNumbers is null, then the limit of results by default it will be 15.
      // Otherwise the limit will be
      // the number passed with a max of 100. That will be the results showed on screen.
      if (resultsNumber == null || resultsNumber == 0) {
        resultsNumber = 15;
      } else if (resultsNumber > 100) {
        resultsNumber = 100;
      }

      // The query returns the list of fieldsVO ordered by it's type and considering the possible
      // label and conditional values
      FieldValue fvPk = fieldRepository.findFirstTypeByIdFieldSchema(idPk);
      if (null == fvPk) {
        fvPk = new FieldValue();
        fvPk.setType(DataType.TEXT);
      }
      // we catch if the query have error pk data and sned notification
      try {
        fieldsVO = fieldRepository.findByIdFieldSchemaWithTagOrdered(idPk, labelSchemaId,
            searchValue, conditionalSchemaId, conditionalValue, fvPk.getType(), resultsNumber);
      } catch (DataIntegrityViolationException e) {

        LOG_ERROR.error(
            "Error with dataset id {}  field  with id {} because data has not correct format {}",
            datasetIdOrigin, idPk, fvPk.getType());
        // we find table and field to send in notification
        Document tableSchema = schemasRepository.findTableSchema(datasetSchemaId,
            fvPk.getRecord().getTableValue().getIdTableSchema());
        Document fieldSchemaDocument =
            schemasRepository.findFieldSchema(datasetSchemaId, fvPk.getIdFieldSchema());
        String tableSchemaName = (String) tableSchema.get("nameTableSchema");
        String tableSchemaId = tableSchema.get("_id").toString();
        String fieldSchemaName = (String) fieldSchemaDocument.get(HEADER_NAME);

        NotificationVO notificationVO = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .datasetId(datasetIdOrigin).datasetSchemaId(datasetSchemaId)
            .tableSchemaId(tableSchemaId).tableSchemaName(tableSchemaName)
            .fieldSchemaId(fvPk.getIdFieldSchema()).fieldSchemaName(fieldSchemaName).build();


        // we send 2 dif notification depends on type
        DatasetTypeEnum type = getDatasetType(datasetIdOrigin);
        EventType eventType =
            DatasetTypeEnum.DESIGN.equals(type) ? EventType.SORT_FIELD_DESIGN_FAILED_EVENT
                : EventType.SORT_FIELD_FAILED_EVENT;


        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
      }
    }
    return fieldsVO;
  }


  /**
   * Gets the referenced dataset id.
   *
   * @param datasetId the dataset id
   * @param idPk the id pk
   *
   * @return the referenced dataset id
   */
  @Override
  public Long getReferencedDatasetId(Long datasetId, String idPk) {
    return datasetMetabaseService.getDatasetDestinationForeignRelation(datasetId, idPk);
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @param outputStream the output stream
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   */
  @Override
  @Transactional
  public void etlExportDataset(@DatasetId Long datasetId, OutputStream outputStream,
      String tableSchemaId, Integer limit, Integer offset, String filterValue, String columnName) {
    try {
      long startTime = System.currentTimeMillis();
      LOG.info("ETL Export process initiated to DatasetId: {}", datasetId);
      exportDatasetETLSQL(datasetId, outputStream, tableSchemaId, limit, offset, filterValue,
          columnName);
      outputStream.flush();
      long endTime = System.currentTimeMillis() - startTime;
      LOG.info("ETL Export process completed for DatasetId: {} in {} seconds", datasetId,
          endTime / 1000);
    } catch (IOException | EEAException e) {
      LOG.error("ETLExport error in  Dataset:", datasetId, e);
    }
  }



  /**
   * Gets the table read only. Receives by parameter the datasetId, the objectId and the type
   * (table, record, field). In example, if receives an objectId that is a Record (that's a record
   * schema id), find the property readOnly of the table that belongs to the record
   *
   * @param datasetId the dataset id
   * @param objectId the object id
   * @param type the type
   *
   * @return the table read only
   */
  @Override
  public Boolean getTableReadOnly(Long datasetId, String objectId, EntityTypeEnum type) {
    Boolean readOnly = false;
    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));

    switch (type) {
      case TABLE:
        readOnly = tableForReadOnly(objectId, readOnly, schema);
        break;
      case RECORD:
        readOnly = recordForReadOnly(objectId, readOnly, schema);
        break;
      case FIELD:
        readOnly = fieldForReadOnly(objectId, readOnly, schema);
        break;
      default:
        break;
    }
    return readOnly;
  }

  /**
   * Checks if is dataset reportable. Dataset is reportable when is designDataset in dataflow with
   * status design or reportingDataset in state Draft.
   *
   * @param idDataset the id dataset
   *
   * @return the boolean
   */
  @Override
  public boolean isDatasetReportable(Long idDataset) {

    boolean result = false;
    DatasetTypeEnum type = datasetMetabaseService.getDatasetType(idDataset);
    DataFlowVO dataflow = getDataflow(idDataset);
    if (DatasetTypeEnum.DESIGN.equals(type) && TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      result = true;
    } else if (DatasetTypeEnum.REPORTING.equals(type)
        || (DatasetTypeEnum.REFERENCE.equals(type)
            && !Boolean.TRUE.equals(referenceDatasetRepository.findById(idDataset)
                .orElse(new ReferenceDataset()).getUpdatable())
            || DatasetTypeEnum.TEST.equals(type))) {
      result = true;
    } else {
      LOG.info("Dataset {} is not reportable because are in dataflow {} and the dataset type is {}",
          idDataset, dataflow.getId(), type);
    }
    return result;
  }



  /**
   * Check if dataset locked or read only.
   *
   * @param datasetId the dataset id
   * @param idRecordSchema the id record schema
   * @param entityType the entity type
   * @return true, if successful
   */
  @Override
  public boolean checkIfDatasetLockedOrReadOnly(Long datasetId, String idRecordSchema,
      EntityTypeEnum entityType) {
    DatasetTypeEnum datasetType = getDatasetType(datasetId);
    Boolean updatable = Boolean.TRUE.equals(referenceDatasetRepository.findById(datasetId)
        .orElse(new ReferenceDataset()).getUpdatable());
    return (DatasetTypeEnum.REFERENCE.equals(datasetType) && Boolean.FALSE.equals(updatable))
        || (!DatasetTypeEnum.DESIGN.equals(datasetType)
            && !(DatasetTypeEnum.REFERENCE.equals(datasetType) && Boolean.TRUE.equals(updatable))
            && Boolean.TRUE.equals(getTableReadOnly(datasetId, idRecordSchema, entityType)));
  }

  /**
   * Copy data.
   *
   * @param dictionaryOriginTargetDatasetsId the dictionary origin target datasets id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  @Override
  public void copyData(Map<Long, Long> dictionaryOriginTargetDatasetsId,
      Map<String, String> dictionaryOriginTargetObjectId) {
    // We've got the dictionary of origin datasetsId and it's new equivalent datasetId from the
    // copied ones
    // We'll load the data from the origin datasetId, modify it using the dictionary to accurate to
    // the target datasetId (like the tableschema) and finally save it
    dictionaryOriginTargetDatasetsId.forEach((Long originDataset, Long targetDataset) -> {

      DesignDataset originDesign =
          designDatasetRepository.findById(originDataset).orElse(new DesignDataset());
      DataSetSchema schema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(originDesign.getDatasetSchema()));
      if (StringUtils.isNoneBlank(originDesign.getDatasetSchema())) {

        List<TableSchema> listOfTablesFiltered = getTablesFromSchema(schema);
        // if there are tables of the origin dataset with tables ToPrefill, then we'll copy the data
        if (!listOfTablesFiltered.isEmpty()) {
          LOG.info("There are data to copy. Copy data from datasetId {} to datasetId {}",
              originDataset, targetDataset);
          List<RecordValue> recordDesignValuesList = new ArrayList<>();
          List<AttachmentValue> attachments = new ArrayList<>();
          recordDesignValuesList = replaceData(schema, originDataset, targetDataset,
              listOfTablesFiltered, dictionaryOriginTargetObjectId, attachments);

          if (!recordDesignValuesList.isEmpty()) {
            // save values
            TenantResolver
                .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, targetDataset));
            recordRepository.saveAll(recordDesignValuesList);
            // copy attachments too
            if (!attachments.isEmpty()) {
              attachmentRepository.saveAll(attachments);
            }
          }
        }
      }
    });
  }

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the attachment
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public AttachmentValue getAttachment(Long datasetId, String idField) throws EEAException {
    return attachmentRepository.findByFieldValueId(idField);
  }

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteAttachment(Long datasetId, String fieldId) throws EEAException {
    // Delete the attachment
    attachmentRepository.deleteByFieldValueId(fieldId);
    // Put the field value name to null
    FieldValue field = fieldRepository.findById(fieldId);
    field.setValue("");
    fieldRepository.save(field);
  }

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   * @param fileName the file name
   * @param is the is
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public void updateAttachment(Long datasetId, String fieldId, String fileName, InputStream is)
      throws EEAException, IOException {

    FieldValue field = fieldRepository.findById(fieldId);
    // Check if the field is marked as read only
    Document fieldSchema = schemasRepository.findFieldSchema(
        field.getRecord().getTableValue().getDatasetId().getIdDatasetSchema(),
        field.getIdFieldSchema());
    if (!DatasetTypeEnum.DESIGN.equals(getDatasetType(datasetId)) && fieldSchema != null
        && fieldSchema.get(LiteralConstants.READ_ONLY) != null
        && fieldSchema.getBoolean(LiteralConstants.READ_ONLY)) {
      throw new EEAException(EEAErrorMessage.FIELD_READ_ONLY);
    }
    // Attachment table
    AttachmentValue attachment = attachmentRepository.findByFieldValueId(fieldId);
    if (null == attachment) {
      attachment = new AttachmentValue();
      attachment.setFieldValue(field);
    }
    attachment.setFileName(fileName);
    byte[] content;
    content = IOUtils.toByteArray(is);
    is.close();
    attachment.setContent(content);
    attachmentRepository.save(attachment);

    // Field table
    field.setValue(fileName);
    fieldRepository.save(field);
  }

  /**
   * Gets the field by id.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the field by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public FieldVO getFieldById(Long datasetId, String idField) throws EEAException {
    FieldValue fieldValue = fieldRepository.findById(idField);
    if (fieldValue == null) {
      throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);
    }
    return fieldNoValidationMapper.entityToClass(fieldValue);
  }

  /**
   * Delete attachment by field schema id.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteAttachmentByFieldSchemaId(Long datasetId, String fieldSchemaId)
      throws EEAException {
    // Delete the attachment
    attachmentRepository.deleteByFieldValueIdFieldSchema(fieldSchemaId);
    // Put the field value name to null
    fieldRepository.clearFieldValue(fieldSchemaId);
  }


  /**
   * Gets the table fixed number of records.
   *
   * @param datasetId the dataset id
   * @param objectId the object id
   * @param type the type
   *
   * @return the table fixed number of records
   */
  @Override
  public Boolean getTableFixedNumberOfRecords(Long datasetId, String objectId,
      EntityTypeEnum type) {
    Boolean fixedNumber = false;
    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));

    switch (type) {
      case TABLE:
        fixedNumber = tableForFixedNumberOfRecords(objectId, fixedNumber, schema);
        break;
      case RECORD:
        fixedNumber = recordForFixedNumberOfRecords(objectId, fixedNumber, schema);
        break;
      case FIELD:
      default:
        break;
    }
    return fixedNumber;
  }


  /**
   * Find record schema id by id.
   *
   * @param datasetId the dataset id
   * @param idRecord the id record
   *
   * @return the string
   */
  @Override
  public String findRecordSchemaIdById(Long datasetId, String idRecord) {
    RecordValue record = recordRepository.findById(idRecord);
    return record.getIdRecordSchema();
  }


  /**
   * Find field schema id by id.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the string
   */
  @Override
  public String findFieldSchemaIdById(Long datasetId, String idField) {
    FieldValue field = fieldRepository.findById(idField);
    return field.getIdFieldSchema();
  }


  /**
   * Spread data prefill.
   *
   * @param datasetSchema the dataset schema
   * @param originId the origin id
   * @param targetDataset the target dataset
   */
  private void spreadDataPrefill(DataSetSchema datasetSchema, Long originId,
      DataSetMetabase targetDataset) {
    // get tables from schema
    List<TableSchema> listOfTablesFiltered = getTablesFromSchema(datasetSchema);
    // get the data from designs datasets
    if (!listOfTablesFiltered.isEmpty()) {
      TenantResolver.setTenantName(String.format(DATASET_ID, originId));
      List<RecordValue> targetRecords = new ArrayList<>();

      // fill the data
      DatasetValue ds = new DatasetValue();
      ds.setId(targetDataset.getId());

      Optional<PartitionDataSetMetabase> datasetPartition =
          partitionDataSetMetabaseRepository.findFirstByIdDataSet_id(targetDataset.getId());
      Long datasetPartitionId = datasetPartition.orElse(new PartitionDataSetMetabase()).getId();
      // attachment values
      List<AttachmentValue> attachments = new ArrayList<>();
      Iterable<AttachmentValue> iterableAttachments = attachmentRepository.findAll();
      iterableAttachments.forEach(attachments::add);
      recordDesingAssignation(targetDataset, originId, targetRecords, datasetPartitionId,
          attachments, listOfTablesFiltered);
      if (!targetRecords.isEmpty()) {
        // save values
        TenantResolver.setTenantName(String.format(DATASET_ID, targetDataset.getId()));
        recordRepository.saveAll(targetRecords);
        // copy attachments too
        if (!attachments.isEmpty()) {
          attachmentRepository.saveAll(attachments);
        }
      }
    }
  }


  /**
   * Creates the lock with signature.
   *
   * @param lockSignature the lock signature
   * @param mapCriteria the map criteria
   * @param userName the user name
   * @throws EEAException the EEA exception
   */
  @Override
  public void createLockWithSignature(LockSignature lockSignature, Map<String, Object> mapCriteria,
      String userName) throws EEAException {
    mapCriteria.put("signature", lockSignature.getValue());
    LockVO lockVO = lockService.findByCriteria(mapCriteria);
    if (lockVO == null) {
      lockService.createLock(new Timestamp(System.currentTimeMillis()), userName, LockType.METHOD,
          mapCriteria);
    }
  }


  /**
   * Record desing assignation.
   *
   * @param targetDataset the target dataset
   * @param originId the origin id
   * @param targetRecords the record design values list
   * @param datasetPartitionId the dataset partition id
   * @param attachments the attachments
   * @param listOfTablesFiltered the list of tables filtered
   */
  private void recordDesingAssignation(DataSetMetabase targetDataset, Long originId,
      List<RecordValue> targetRecords, Long datasetPartitionId, List<AttachmentValue> attachments,
      List<TableSchema> listOfTablesFiltered) {

    Map<String, AttachmentValue> dictionaryIdFieldAttachment = new HashMap<>();
    attachments.forEach(attachment -> {
      if (null != attachment.getFieldValue() && null != attachment.getFieldValue().getId()) {
        dictionaryIdFieldAttachment.put(attachment.getFieldValue().getId(), attachment);
      }
    });

    Long dataProviderId = targetDataset.getDataProviderId();
    final DataProviderVO dataproviderVO =
        null != dataProviderId ? representativeControllerZuul.findDataProviderById(dataProviderId)
            : new DataProviderVO();

    for (TableSchema tableSchema : listOfTablesFiltered) {
      TenantResolver.setTenantName(
          String.format(LiteralConstants.DATASET_FORMAT_NAME, targetDataset.getId()));

      TableValue targetTable =
          tableRepository.findByIdTableSchema(tableSchema.getIdTableSchema().toString());

      TenantResolver
          .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, originId.toString()));
      if (Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        LOG.info("Processing Table {} with table schema {} from Dataset {} and Target Dataset {} ",
            tableSchema.getNameTableSchema(), tableSchema.getIdTableSchema(), originId,
            targetDataset.getId());
        processRecordPageWithFixedRecords(dictionaryIdFieldAttachment, targetTable,
            datasetPartitionId, null, targetDataset.getId(), originId, dataproviderVO);
      } else {
        Integer numberOfFieldsInRecord = tableSchema.getRecordSchema().getFieldSchema().size();

        Pageable fieldValuePage = PageRequest.of(0, 1000 * numberOfFieldsInRecord);

        List<FieldValue> pagedFieldValues;
        Map<String, RecordValue> mapTargetRecordValues = new HashMap<>();
        while (!(pagedFieldValues = fieldRepository.findByRecord_IdRecordSchema(
            tableSchema.getRecordSchema().getIdRecordSchema().toString(), fieldValuePage))
                .isEmpty()) {

          processRecordPage(pagedFieldValues, targetRecords, mapTargetRecordValues,
              dictionaryIdFieldAttachment, targetTable, numberOfFieldsInRecord, dataproviderVO,
              datasetPartitionId, null);
          fieldValuePage = fieldValuePage.next();
        }
      }


    }

  }

  /**
   * Process record page.
   *
   * @param pagedFieldValues the paged field values
   * @param targetRecords the target records
   * @param mapTargetRecordValues the map target record values
   * @param dictionaryIdFieldAttachment the dictionary id field attachment
   * @param targetTable the target table
   * @param numberOfFieldsInRecord the number of fields in record
   * @param dataproviderVO the dataprovider VO
   * @param datasetPartitionId the dataset partition id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  // Method invoked from recordDesingAssignation and replaceData methods, reducing duplicated code
  // and Cognitive Complexity
  private void processRecordPage(List<FieldValue> pagedFieldValues, List<RecordValue> targetRecords,
      Map<String, RecordValue> mapTargetRecordValues,
      Map<String, AttachmentValue> dictionaryIdFieldAttachment, TableValue targetTable,
      Integer numberOfFieldsInRecord, DataProviderVO dataproviderVO, Long datasetPartitionId,
      Map<String, String> dictionaryOriginTargetObjectId) {
    // make list of field vaues grouped by their record id. The field values will be set with
    // the taget schemas id so they can be inserted
    pagedFieldValues.stream().forEach(field -> {
      FieldValue auxField = new FieldValue();
      auxField.setValue(field.getValue());
      String targetIdRecordSchema = null;

      if (null == dictionaryOriginTargetObjectId) {// called from recordDesingAssignation
        auxField.setIdFieldSchema(field.getIdFieldSchema());
        targetIdRecordSchema = field.getRecord().getIdRecordSchema();
      } else {// called from replaceData
        auxField.setIdFieldSchema(dictionaryOriginTargetObjectId.get(field.getIdFieldSchema()));
        // transform the grouping record in the target one. Do it only once
        targetIdRecordSchema =
            dictionaryOriginTargetObjectId.get(field.getRecord().getIdRecordSchema());
      }

      auxField.setType(field.getType());

      String originRecordId = field.getRecord().getId();
      if (!mapTargetRecordValues.containsKey(originRecordId)) {

        RecordValue targetRecordValue = new RecordValue();
        if (null != dataproviderVO) { // called from recordDesingAssignation
          targetRecordValue.setDataProviderCode(dataproviderVO.getCode());
        }
        targetRecordValue.setDatasetPartitionId(datasetPartitionId);
        targetRecordValue.setIdRecordSchema(targetIdRecordSchema);
        targetRecordValue.setTableValue(targetTable);
        targetRecordValue.setFields(new ArrayList<>());
        // using temporary recordId as grouping criteria, then it will be removed before giving
        // back
        mapTargetRecordValues.put(originRecordId, targetRecordValue);
        targetRecords.add(targetRecordValue);
      }

      auxField.setRecord(mapTargetRecordValues.get(originRecordId));

      if (DataType.ATTACHMENT.equals(field.getType())
          && dictionaryIdFieldAttachment.containsKey(field.getId())) {
        dictionaryIdFieldAttachment.get(field.getId()).setFieldValue(auxField);
        dictionaryIdFieldAttachment.get(field.getId()).setId(null);

      }
      auxField.setGeometry(field.getGeometry());
      mapTargetRecordValues.get(originRecordId).getFields().add(auxField);
      // when the record has reached the number of fields per record then remove from the map to
      // avoid rehashing
      if (mapTargetRecordValues.get(originRecordId).getFields().size() == numberOfFieldsInRecord) {
        mapTargetRecordValues.remove(originRecordId);
      }
    });
  }

  /**
   * Gets the tables from schema.
   *
   * @param schema the schema
   * @return the tables from schema
   */
  private List<TableSchema> getTablesFromSchema(DataSetSchema schema) {
    List<TableSchema> listOfTables = schema.getTableSchemas();
    List<TableSchema> listOfTablesFiltered = new ArrayList<>();
    for (TableSchema desingTableToPrefill : listOfTables) {
      if (Boolean.TRUE.equals(desingTableToPrefill.getToPrefill())) {
        listOfTablesFiltered.add(desingTableToPrefill);
      }
    }
    return listOfTablesFiltered;
  }

  /**
   * Gets the dataflow.
   *
   * @param idDataset the id dataset
   *
   * @return the dataflow
   */
  private DataFlowVO getDataflow(Long idDataset) {
    // Get the dataFlowId from the metabase
    Long dataflowId = getDataFlowIdById(idDataset);
    // get de dataflow
    return dataflowControllerZuul.getMetabaseById(dataflowId);
  }

  /**
   * Retrieve validations.
   *
   * @param recordVOs the record V os
   */
  private void retrieveValidations(List<RecordVO> recordVOs) {
    // retrieve validations to set them into the final result
    List<String> recordIds = recordVOs.stream().map(RecordVO::getId).collect(Collectors.toList());
    Map<String, List<FieldValidation>> fieldValidations = this.getFieldValidations(recordIds);
    Map<String, List<RecordValidation>> recordValidations = this.getRecordValidations(recordIds);
    recordVOs.stream().forEach(record -> {
      record.getFields().parallelStream().forEach(field -> {
        List<FieldValidationVO> validations =
            fieldValidationMapper.entityListToClass(fieldValidations.get(field.getId()));
        field.setFieldValidations(validations);
        if (null != validations && !validations.isEmpty()) {
          field.setLevelError(
              validations.stream().map(validation -> validation.getValidation().getLevelError())
                  .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                  .orElse(ErrorTypeEnum.WARNING));
        }
      });

      List<RecordValidationVO> validations =
          recordValidationMapper.entityListToClass(recordValidations.get(record.getId()));
      record.setRecordValidations(validations);
      if (null != validations && !validations.isEmpty()) {
        record.setLevelError(
            validations.stream().map(validation -> validation.getValidation().getLevelError())
                .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                .orElse(ErrorTypeEnum.WARNING));
      }
    });
  }

  /**
   * Calculate pageable.
   *
   * @param pageable the pageable
   * @param totalRecords the total records
   *
   * @return the pageable
   */
  private Pageable calculatePageable(Pageable pageable, Long totalRecords) {
    if (pageable == null && totalRecords > 0) {
      pageable = PageRequest.of(0, totalRecords.intValue());
    }
    return pageable;
  }

  /**
   * Calculated errors and records to see.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param commonShortFields the common short fields
   * @param mapFields the map fields
   * @param sortFieldsArray the sort fields array
   * @param newFields the new fields
   * @param result the result
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table VO
   */
  private TableVO calculatedErrorsAndRecordsToSee(final Long datasetId, final String idTableSchema,
      Pageable pageable, final String fields, ErrorTypeEnum[] levelError,
      List<String> commonShortFields, Map<String, Integer> mapFields,
      List<SortField> sortFieldsArray, SortField[] newFields, TableVO result, String[] idRules,
      String fieldSchema, String fieldValue) {
    List<RecordValue> records;
    if (null == fields && (null == levelError || levelError.length == 5)
        && (idRules == null || idRules.length == 0) && fieldSchema == null && fieldValue == null) {
      records = recordRepository.findByTableValueNoOrder(idTableSchema, pageable);
      List<RecordVO> recordVOs = recordNoValidationMapper.entityListToClass(records);
      result.setTotalFilteredRecords(0L);
      result.setRecords(recordVOs);
    } else {
      result = fieldsMap(datasetId, idTableSchema, pageable, fields, levelError, commonShortFields,
          mapFields, sortFieldsArray, newFields, idRules, fieldSchema, fieldValue);
    }
    return result;
  }

  /**
   * Fields map.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param commonShortFields the common short fields
   * @param mapFields the map fields
   * @param sortFieldsArray the sort fields array
   * @param newFields the new fields
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table VO
   */
  private TableVO fieldsMap(final Long datasetId, final String idTableSchema, Pageable pageable,
      final String fields, ErrorTypeEnum[] levelError, List<String> commonShortFields,
      Map<String, Integer> mapFields, List<SortField> sortFieldsArray, SortField[] newFields,
      String[] idRules, String fieldSchema, String fieldValue) {
    TableVO result;
    if (null != fields) {

      String[] pairs = fields.split(",");
      for (int i = 0; i < pairs.length; i++) {
        String pair = pairs[i];
        String[] keyValue = pair.split(":");
        mapFields.put(keyValue[0], Integer.valueOf(keyValue[1]));
        commonShortFields.add(keyValue[0]);
      }

      for (String nameField : commonShortFields) {
        FieldValue typefield = fieldRepository.findFirstTypeByIdFieldSchema(nameField);
        SortField sortField = new SortField();
        sortField.setFieldName(nameField);
        sortField.setAsc(mapFields.get(nameField) == 1);
        if (null == typefield) {
          sortField.setTypefield(DataType.TEXT);
        } else {
          sortField.setTypefield(typefield.getType());
        }

        sortFieldsArray.add(sortField);
      }
      newFields = sortFieldsArray.stream().toArray(SortField[]::new);
    }

    result = recordRepository.findByTableValueWithOrder(datasetId, idTableSchema,
        Arrays.asList(levelError), pageable, idRules != null ? Arrays.asList(idRules) : null,
        fieldSchema, fieldValue, newFields);
    return result;
  }

  /**
   * Retrieve record value.
   *
   * @param idTableSchema the id table schema
   * @param idFieldSchema the id field schema
   *
   * @return the list
   */
  @Deprecated
  private List<RecordValue> retrieveRecordValue(String idTableSchema, String idFieldSchema) {
    Optional.ofNullable(idFieldSchema).ifPresent(field -> SortFieldsHelper.setSortingField(field));
    List<RecordValue> records = null;
    try {
      records = recordRepository.findByTableValueIdTableSchema(idTableSchema);
    } finally {
      SortFieldsHelper.cleanSortingField();
    }

    return records;
  }

  /**
   * Sanitize records.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(final List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    if (records != null && !records.isEmpty()) {
      Set<String> processedRecords = new HashSet<>();
      for (RecordValue recordValue : records) {
        if (!processedRecords.contains(recordValue.getId())) {
          processedRecords.add(recordValue.getId());
          sanitizedRecords.add(recordValue);
        }

      }
    }
    return sanitizedRecords;
  }

  /**
   * Process table stats.
   *
   * @param tableValue the table value
   * @param datasetId the dataset id
   * @param mapIdNameDatasetSchema the map id name dataset schema
   *
   * @return the list
   */
  private List<Statistics> processTableStats(final TableValue tableValue, final Long datasetId,
      final Map<String, String> mapIdNameDatasetSchema) {
    // Find record ids with errors
    List<IDError> recordIdsFromRecordWithValidationBlocker =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema());
    List<IDError> recordIdsFromFieldWithValidationBlocker = recordValidationRepository
        .findRecordIdFromFieldWithValidationsByLevelError(datasetId, tableValue.getIdTableSchema());

    Set<String> idsBlockers = new HashSet<>();
    Set<String> idsErrors = new HashSet<>();
    Set<String> idsWarnings = new HashSet<>();
    Set<String> idsInfos = new HashSet<>();
    // Sort errors by criticality
    recordIdsFromRecordWithValidationBlocker.stream().forEach(keyset -> {
      filterErrors(idsBlockers, idsErrors, idsWarnings, idsInfos, keyset);
    });
    recordIdsFromFieldWithValidationBlocker.stream().forEach(keyset -> {
      filterErrors(idsBlockers, idsErrors, idsWarnings, idsInfos, keyset);
    });

    idsErrors.removeAll(idsBlockers);
    idsWarnings.removeAll(idsBlockers);
    idsWarnings.removeAll(idsErrors);
    idsInfos.removeAll(idsBlockers);
    idsInfos.removeAll(idsErrors);
    idsInfos.removeAll(idsWarnings);

    Long totalRecordsWithBlockers = Long.valueOf(idsBlockers.size());
    Long totalRecordsWithErrors = Long.valueOf(idsErrors.size());
    Long totalRecordsWithWarnings = Long.valueOf(idsWarnings.size());
    Long totalRecordsWithInfos = Long.valueOf(idsInfos.size());

    List<Statistics> stats = new ArrayList<>();
    Long countRecords = tableRepository.countRecordsByIdTableSchema(tableValue.getIdTableSchema());

    Long totalTableErrors =
        totalRecordsWithBlockers + totalRecordsWithErrors + totalRecordsWithWarnings
            + totalRecordsWithInfos + tableValue.getTableValidations().size();
    // Fill different stats
    String tableSchemaId = tableValue.getIdTableSchema();
    stats.add(fillStat(datasetId, tableSchemaId, "idTableSchema", tableValue.getIdTableSchema()));
    stats.add(fillStat(datasetId, tableSchemaId, "nameTableSchema",
        mapIdNameDatasetSchema.get(tableSchemaId)));
    stats.add(fillStat(datasetId, tableSchemaId, "totalErrors", totalTableErrors.toString()));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecords", countRecords.toString()));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithBlockers",
        totalRecordsWithBlockers.toString()));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithErrors",
        totalRecordsWithErrors.toString()));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithWarnings",
        totalRecordsWithWarnings.toString()));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithInfos",
        totalRecordsWithInfos.toString()));

    Statistics statsTableErrors = new Statistics();
    statsTableErrors.setIdTableSchema(tableSchemaId);
    statsTableErrors.setStatName("tableErrors");
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(datasetId);
    statsTableErrors.setDataset(reporting);
    if (tableValue.getTableValidations() != null && !tableValue.getTableValidations().isEmpty()) {
      statsTableErrors.setValue("true");
    } else {
      statsTableErrors.setValue(String.valueOf(totalTableErrors > 0));
    }

    stats.add(statsTableErrors);

    return stats;
  }

  /**
   * Filter errors.
   *
   * @param idsBlockers the ids blockers
   * @param idsErrors the ids errors
   * @param idsWarnings the ids warnings
   * @param idsInfos the ids infos
   * @param keyset the keyset
   */
  private void filterErrors(Set<String> idsBlockers, Set<String> idsErrors, Set<String> idsWarnings,
      Set<String> idsInfos, IDError keyset) {
    switch (keyset.getLevelError()) {
      case "BLOCKER":
        idsBlockers.add(keyset.getId());
        break;
      case "ERROR":
        idsErrors.add(keyset.getId());
        break;
      case "WARNING":
        idsWarnings.add(keyset.getId());
        break;
      case "INFO":
        idsInfos.add(keyset.getId());
        break;
      default:
    }
  }

  /**
   * Fill stat.
   *
   * @param idDataset the id dataset
   * @param idTableSchema the id table schema
   * @param statName the stat name
   * @param value the value
   *
   * @return the statistics
   */
  private Statistics fillStat(Long idDataset, String idTableSchema, String statName, String value) {

    Statistics stat = new Statistics();
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(idDataset);
    stat.setDataset(reporting);
    stat.setIdTableSchema(idTableSchema);
    stat.setStatName(statName);
    stat.setValue(value);

    return stat;
  }

  /**
   * Gets the field validations.
   *
   * @param recordIds the record ids
   *
   * @return the field validations
   */
  private Map<String, List<FieldValidation>> getFieldValidations(final List<String> recordIds) {
    List<FieldValidation> fieldValidations =
        this.fieldValidationRepository.findByFieldValue_RecordIdIn(recordIds);

    Map<String, List<FieldValidation>> result = new HashMap<>();

    fieldValidations.stream().forEach(fieldValidation -> {
      if (!result.containsKey(fieldValidation.getFieldValue().getId())) {
        result.put(fieldValidation.getFieldValue().getId(), new ArrayList<>());
      }
      result.get(fieldValidation.getFieldValue().getId()).add(fieldValidation);
    });

    return result;
  }

  /**
   * Gets the record validations.
   *
   * @param recordIds the record ids
   *
   * @return the record validations
   */
  private Map<String, List<RecordValidation>> getRecordValidations(final List<String> recordIds) {

    List<RecordValidation> recordValidations =
        this.recordValidationRepository.findByRecordValueIdIn(recordIds);

    Map<String, List<RecordValidation>> result = new HashMap<>();

    recordValidations.stream().forEach(recordValidation -> {
      if (!result.containsKey(recordValidation.getRecordValue().getId())) {
        result.put(recordValidation.getRecordValue().getId(), new ArrayList<>());
      }
      result.get(recordValidation.getRecordValue().getId()).add(recordValidation);
    });

    return result;
  }

  /**
   * Table for read only.
   *
   * @param objectId the object id
   * @param readOnly the read only
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean tableForReadOnly(String objectId, Boolean readOnly, DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getIdTableSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        readOnly = true;
        break;
      }
    }
    return readOnly;
  }

  /**
   * Table for fixed number of records.
   *
   * @param objectId the object id
   * @param fixedNumber the fixed number
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean tableForFixedNumberOfRecords(String objectId, Boolean fixedNumber,
      DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getIdTableSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        fixedNumber = true;
        break;
      }
    }
    return fixedNumber;
  }

  /**
   * Record for read only.
   *
   * @param objectId the object id
   * @param readOnly the read only
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean recordForReadOnly(String objectId, Boolean readOnly, DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getRecordSchema().getIdRecordSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        readOnly = true;
        break;
      }
    }
    return readOnly;
  }


  /**
   * Record for fixed number of records.
   *
   * @param objectId the object id
   * @param fixedNumber the fixed number
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean recordForFixedNumberOfRecords(String objectId, Boolean fixedNumber,
      DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getRecordSchema().getIdRecordSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        fixedNumber = true;
        break;
      }
    }
    return fixedNumber;
  }


  /**
   * Field for read only.
   *
   * @param objectId the object id
   * @param readOnly the read only
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean fieldForReadOnly(String objectId, Boolean readOnly, DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
        if (objectId.equals(fieldSchema.getIdFieldSchema().toString())
            && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
          readOnly = true;
          break;
        }
      }
    }
    return readOnly;
  }

  /**
   * Replace data.
   *
   * @param schema the schema
   * @param originDataset the origin dataset
   * @param targetDataset the target dataset
   * @param listOfTablesFiltered the list of tables filtered
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param attachments the attachments
   * @return the list
   */
  private List<RecordValue> replaceData(DataSetSchema schema, Long originDataset,
      Long targetDataset, List<TableSchema> listOfTablesFiltered,
      Map<String, String> dictionaryOriginTargetObjectId, List<AttachmentValue> attachments) {
    List<RecordValue> result = new ArrayList<>();
    TenantResolver.setTenantName(
        String.format(LiteralConstants.DATASET_FORMAT_NAME, originDataset.toString()));

    // attachment values
    Iterable<AttachmentValue> iterableAttachments = attachmentRepository.findAll();
    Map<String, AttachmentValue> dictionaryIdFieldAttachment = new HashMap<>();
    iterableAttachments.forEach(attachment -> {
      if (null != attachment.getFieldValue() && null != attachment.getFieldValue().getId()) {
        dictionaryIdFieldAttachment.put(attachment.getFieldValue().getId(), attachment);
      }
    });

    // fill the data
    DatasetValue ds = new DatasetValue();
    ds.setId(targetDataset);

    Optional<PartitionDataSetMetabase> datasetPartition =
        partitionDataSetMetabaseRepository.findFirstByIdDataSet_id(targetDataset);
    final Long datasetPartitionId =
        datasetPartition.isPresent() ? datasetPartition.get().getId() : null;

    Map<String, RecordValue> mapTargetRecordValues = new HashMap<>();
    for (TableSchema desingTable : listOfTablesFiltered) {
      // Get number of fields per record. Doing it on origin table schema as origin and target has
      // the same structure
      Integer numberOfFieldsInRecord = desingTable.getRecordSchema().getFieldSchema().size();

      // get target table by translating origing table schema into target table schema and then
      // query to target database
      TenantResolver.setTenantName(
          String.format(LiteralConstants.DATASET_FORMAT_NAME, targetDataset.toString()));
      TableValue targetTable = tableRepository.findByIdTableSchema(
          dictionaryOriginTargetObjectId.get(desingTable.getIdTableSchema().toString()));

      TenantResolver.setTenantName(
          String.format(LiteralConstants.DATASET_FORMAT_NAME, originDataset.toString()));

      // creating a first page of 1000 records, this means 1000*Number Of Fields in a Record

      if (Boolean.TRUE.equals(desingTable.getFixedNumber())) {
        LOG.info("Processing Table {} with table schema {} from Dataset {} and Target Dataset {} ",
            desingTable.getNameTableSchema(), desingTable.getIdTableSchema(), originDataset,
            targetDataset);
        processRecordPageWithFixedRecords(dictionaryIdFieldAttachment, targetTable,
            datasetPartitionId, dictionaryOriginTargetObjectId, targetDataset, originDataset, null);
      } else {
        // creating a first page of 1000 records, this means 1000*Number Of Fields in a Record

        Pageable fieldValuePage = PageRequest.of(0, 1000 * numberOfFieldsInRecord);

        List<FieldValue> pagedFieldValues;

        // run through the origin table, getting its records and fields and translating them into
        // the
        // new schema
        while (!(pagedFieldValues = fieldRepository.findByRecord_IdRecordSchema(
            desingTable.getRecordSchema().getIdRecordSchema().toString(), fieldValuePage))
                .isEmpty()) {
          LOG.info(
              "Processing page {} with {} records of {} fields from Table {} with table schema {} from Dataset {} and Target Dataset {} ",
              fieldValuePage.getPageNumber(), pagedFieldValues.size() / numberOfFieldsInRecord,
              numberOfFieldsInRecord, desingTable.getNameTableSchema(),
              desingTable.getIdTableSchema(), originDataset, targetDataset);
          // For this, the best is getting fields in big completed sets and assign them to the
          // records
          // to avoid excessive queries to bd

          processRecordPage(pagedFieldValues, result, mapTargetRecordValues,
              dictionaryIdFieldAttachment, targetTable, numberOfFieldsInRecord, null,
              datasetPartitionId, dictionaryOriginTargetObjectId);

          fieldValuePage = fieldValuePage.next();
          TenantResolver.setTenantName(
              String.format(LiteralConstants.DATASET_FORMAT_NAME, originDataset.toString()));
        }
      }
    }

    attachments.addAll(dictionaryIdFieldAttachment.values());
    return result;
  }

  /**
   * Process record page with fixed records.
   *
   * @param dictionaryIdFieldAttachment the dictionary id field attachment
   * @param targetTable the target table
   * @param datasetPartitionId the dataset partition id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param targetDatasetId the target dataset id
   * @param originDatasetId the origin dataset id
   * @param dataproviderVO the dataprovider VO
   */
  private void processRecordPageWithFixedRecords(
      Map<String, AttachmentValue> dictionaryIdFieldAttachment, TableValue targetTable,
      Long datasetPartitionId, Map<String, String> dictionaryOriginTargetObjectId,
      Long targetDatasetId, Long originDatasetId, DataProviderVO dataproviderVO) {
    try {
      List<RecordValue> auxRecords = new ArrayList<>();
      for (RecordValue record : recordRepository.findOrderedNativeRecord(targetTable.getId(),
          originDatasetId, null)) {
        RecordValue recordAux = new RecordValue();
        BeanUtils.copyProperties(recordAux, record);
        recordAux.setId(null);
        recordAux.setTableValue(targetTable);
        recordAux.setDatasetPartitionId(datasetPartitionId);
        if (dictionaryOriginTargetObjectId != null) {
          recordAux
              .setIdRecordSchema(dictionaryOriginTargetObjectId.get(recordAux.getIdRecordSchema()));
        }
        if (null != dataproviderVO) {
          recordAux.setDataProviderCode(dataproviderVO.getCode());
        }
        List<FieldValue> fields = new ArrayList<>();
        for (FieldValue field : record.getFields()) {
          FieldValue fieldAux = new FieldValue();
          BeanUtils.copyProperties(fieldAux, field);
          if (dictionaryOriginTargetObjectId != null) {
            fieldAux.setIdFieldSchema(dictionaryOriginTargetObjectId.get(field.getIdFieldSchema()));
          }
          if (DataType.ATTACHMENT.equals(field.getType())
              && dictionaryIdFieldAttachment.containsKey(field.getId())) {
            dictionaryIdFieldAttachment.get(field.getId()).setFieldValue(field);
            dictionaryIdFieldAttachment.get(field.getId()).setId(null);
          }
          fieldAux.setId(null);
          fieldAux.setRecord(recordAux);
          fields.add(fieldAux);
        }
        recordAux.setFields(fields);
        auxRecords.add(recordAux);
      }
      TenantResolver
          .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, targetDatasetId));
      saveAllRecords(auxRecords);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error("error processing records with dataset origin: {}", originDatasetId, e);
    }

  }

  /**
   * Save all records.
   *
   * @param auxRecords the aux records
   */
  @Transactional
  public void saveAllRecords(List<RecordValue> auxRecords) {
    recordRepository.saveAll(auxRecords);
  }

  /**
   * Creates the records.
   *
   * @param datasetId the dataset id
   * @param dataProviderCode the data provider code
   * @param recordVOs the record V os
   * @param datasetType the dataset type
   * @param tableSchema the table schema
   *
   * @return the list
   */
  private List<RecordValue> createRecords(Long datasetId, String dataProviderCode,
      List<RecordVO> recordVOs, DatasetTypeEnum datasetType, TableSchema tableSchema) {

    String tableSchemaId = tableSchema.getIdTableSchema().toString();
    Long datasetPartitionId =
        partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(datasetId, USER)
            .orElse(new PartitionDataSetMetabase()).getId();

    DatasetValue dataset = new DatasetValue();
    dataset.setId(datasetId);

    TableValue tableValue = new TableValue();
    tableValue.setId(tableRepository.findIdByIdTableSchema(tableSchemaId));
    tableValue.setDatasetId(dataset);

    List<RecordValue> recordValues = new ArrayList<>();

    // Rebuild each record to ensure it contains proper fields
    for (RecordVO recordVO : recordVOs) {
      List<FieldVO> fieldVOs = recordVO.getFields();
      List<FieldValue> fieldValues = new ArrayList<>();

      RecordValue recordValue = new RecordValue();
      recordValue.setDataProviderCode(dataProviderCode);
      recordValue.setDatasetPartitionId(datasetPartitionId);
      recordValue.setTableValue(tableValue);
      recordValue.setFields(fieldValues);
      recordValue.setIdRecordSchema(tableSchema.getRecordSchema().getIdRecordSchema().toString());
      recordValues.add(recordValue);

      for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
        FieldValue fieldValue = new FieldValue();
        fieldValue.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
        fieldValue.setType(fieldSchema.getType());
        fieldValue.setValue(filterFieldValue(fieldSchema, datasetType, fieldVOs));
        fieldValue.setRecord(recordValue);
        fieldValues.add(fieldValue);
      }
    }
    return recordValues;
  }

  /**
   * Filter field value.
   *
   * @param fieldSchema the field schema
   * @param datasetType the dataset type
   * @param fieldVOs the field V os
   *
   * @return the string
   */
  private String filterFieldValue(FieldSchema fieldSchema, DatasetTypeEnum datasetType,
      List<FieldVO> fieldVOs) {

    String value = "";
    String fieldSchemaId = fieldSchema.getIdFieldSchema().toString();
    DataType dataType = fieldSchema.getType();

    // Skip if write is not allowed
    if (Boolean.TRUE.equals(fieldSchema.getReadOnly())
        && !DatasetTypeEnum.DESIGN.equals(datasetType)) {
      return value;
    }

    // Find the fieldVO with the fieldSchemaId if exists
    for (FieldVO fieldVO : fieldVOs) {
      if (fieldSchemaId.equals(fieldVO.getIdFieldSchema())) {
        if (null != fieldVO.getValue()) {
          value = fieldVO.getValue();

          // Sort values if there are multiple
          if (DataType.MULTISELECT_CODELIST.equals(dataType)
              || (DataType.LINK.equals(dataType) || DataType.EXTERNAL_LINK.equals(dataType)
                  && Boolean.TRUE.equals(fieldSchema.getPkHasMultipleValues()))) {
            String[] values = value.trim().split("\\s*;\\s*");
            Arrays.sort(values);
            value = Arrays.stream(values).collect(Collectors.joining("; "));
          }
        }
        fieldVOs.remove(fieldVO);
        break;
      }
    }

    // Cut string value to maximum length
    switch (dataType) {
      case POINT:
        break;
      case LINESTRING:
        break;
      case POLYGON:
        break;
      case MULTIPOINT:
        break;
      case MULTILINESTRING:
        break;
      case MULTIPOLYGON:
        break;
      case GEOMETRYCOLLECTION:
        break;
      default:
        if (value.length() >= fieldMaxLength) {
          value = value.substring(0, fieldMaxLength);
        }
    }

    return value;
  }

  /**
   * Gets the dataset type, if it's a design, reporting, datacollection or eudataset .
   *
   * @param datasetId the dataset id
   *
   * @return the dataset type
   */
  @Override
  @Cacheable(value = "datasetType", key = "#datasetId")
  public DatasetTypeEnum getDatasetType(Long datasetId) {
    DatasetTypeEnum type = null;
    if (reportingDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.REPORTING;
    } else if (designDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.DESIGN;
    } else if (testDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.TEST;
    } else if (referenceDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.REFERENCE;
    } else if (dataCollectionRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.COLLECTION;
    } else if (dataSetMetabaseRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.EUDATASET;
    }

    return type;
  }

  /**
   * Update cascade PK.
   *
   * @param recordValueId the record value id
   * @param fieldValues the field values
   *
   * @throws EEAException the EEA exception
   */
  private void updateCascadePK(final String recordValueId, final List<FieldValue> fieldValues)
      throws EEAException {

    // we took recordValue to take more information necesary to calculate pk
    RecordValue record = recordRepository.findById(recordValueId);

    // we bring the schema
    Document recordSchemaDocument = schemasRepository.findRecordSchema(
        record.getTableValue().getDatasetId().getIdDatasetSchema(),
        record.getTableValue().getIdTableSchema());
    // get fks to delete
    if (null != recordSchemaDocument) {
      List<?> fieldSchemasList =
          (ArrayList<?>) recordSchemaDocument.get(LiteralConstants.FIELD_SCHEMAS);

      Document fieldSchemaDocument = null;
      String idListOfSinglePamsField = null;
      for (Object document : fieldSchemasList) {
        if (((Document) document).get(LiteralConstants.PK) != null
            && ((Document) document).getBoolean(LiteralConstants.PK)) {
          fieldSchemaDocument = (Document) document;
        }
        if (((Document) document).get(HEADER_NAME) != null
            && ((Document) document).getString(HEADER_NAME).equals("ListOfSinglePams")) {
          idListOfSinglePamsField = ((Document) document).get("_id").toString();
        }
      }
      // we look if the record to update have any pk
      if (null != fieldSchemaDocument) {
        updatePKsValues(fieldValues, fieldSchemaDocument, idListOfSinglePamsField);
      }
    }
  }

  /**
   * Update Pks values in cascade when we update the id.
   *
   * @param fieldValues the field values
   * @param fieldSchemaDocument the field schema document
   * @param idListOfSinglePamsField the id list of single pams field
   *
   * @throws EEAException the EEA exception
   */
  private void updatePKsValues(final List<FieldValue> fieldValues, Document fieldSchemaDocument,
      String idListOfSinglePamsField) throws EEAException {
    // we took the both fields, database and filled in new record
    String idFieldSchema = fieldSchemaDocument.get(LiteralConstants.ID).toString();
    FieldValue fieldValueInRecord = fieldValues.stream()
        .filter(field -> field.getIdFieldSchema().equals(idFieldSchema)).findFirst().orElse(null);

    // we find if exist for this pk the same value in pk(in another field) and throw a error if
    // exist
    if (null != fieldValueInRecord) {
      FieldValue fieldValueDatabaseEquals = fieldRepository.findOneByIdFieldSchemaAndValue(
          fieldValueInRecord.getIdFieldSchema(), fieldValueInRecord.getValue());

      if (null != fieldValueDatabaseEquals
          && !fieldValueDatabaseEquals.getId().equalsIgnoreCase(fieldValueInRecord.getId())) {
        throw new EEAException(
            String.format(EEAErrorMessage.PK_ID_ALREADY_EXIST, fieldValueInRecord.getValue()));
      }
      FieldValue fieldValueDatabase = fieldRepository.findById(fieldValueInRecord.getId());
      // we compare if that value is diferent, if not we ignore the pk cascade
      PkCatalogueSchema pkCatalogueSchema =
          pkCatalogueRepository.findByIdPk(new ObjectId(idFieldSchema));
      if (null != pkCatalogueSchema && null != pkCatalogueSchema.getReferenced()) {
        List<String> referenced = pkCatalogueSchema.getReferenced().stream().map(ObjectId::toString)
            .collect(Collectors.toList());
        List<FieldValue> fieldsValues = fieldRepository.findByIdFieldSchemaIn(referenced);
        // we save if the data are the same th
        fieldsValues.stream().forEach(fieldValuePkOtherTable -> {
          if (fieldValueDatabase.getValue().equalsIgnoreCase(fieldValuePkOtherTable.getValue())) {
            fieldValuePkOtherTable.setValue(fieldValueInRecord.getValue());
            fieldRepository.saveValue(fieldValuePkOtherTable.getId(),
                fieldValuePkOtherTable.getValue());
          }
        });
      }
      // we call pams service
      paMService.updateGroups(idListOfSinglePamsField, fieldValueDatabase, fieldValueInRecord);
    }
  }

  /**
   * Field value update PK. we change in cascade when we update a pk value
   *
   * @param fieldValueVO the field value VO
   * @param fieldSchemaDocument the field schema document
   * @param datasetSchemaId the dataset schema id
   *
   * @throws EEAException the EEA exception
   */
  private void fieldValueUpdatePK(final FieldVO fieldValueVO, final Document fieldSchemaDocument,
      final String datasetSchemaId) throws EEAException {

    // we find if exist for this pk the same value in pk and throw a error if exist
    FieldValue fieldValueDatabaseEquals = fieldRepository
        .findOneByIdFieldSchemaAndValue(fieldValueVO.getIdFieldSchema(), fieldValueVO.getValue());
    if (null != fieldValueDatabaseEquals
        && !fieldValueDatabaseEquals.getId().equalsIgnoreCase(fieldValueVO.getId())) {
      throw new EEAException(
          String.format(EEAErrorMessage.PK_ID_ALREADY_EXIST, fieldValueVO.getValue()));
    }
    // we took fieldValue to take more information necesary to calculate pk
    FieldValue fieldValue = fieldRepository.findById(fieldValueVO.getId());

    // we bring the schema
    Document recordSchemaDocument = schemasRepository.findRecordSchema(datasetSchemaId,
        fieldValue.getRecord().getTableValue().getIdTableSchema());
    // get fks to delete
    if (null != recordSchemaDocument) {
      List<Document> fieldSchemasList =
          (ArrayList) recordSchemaDocument.get(LiteralConstants.FIELD_SCHEMAS);

      String idFieldSchema = fieldSchemaDocument.get(LiteralConstants.ID).toString();
      PkCatalogueSchema pkCatalogueSchema =
          pkCatalogueRepository.findByIdPk(new ObjectId(idFieldSchema));

      if (null != pkCatalogueSchema && null != pkCatalogueSchema.getReferenced()) {
        List<String> referenced = pkCatalogueSchema.getReferenced().stream().map(ObjectId::toString)
            .collect(Collectors.toList());
        // we update the rest of pk
        List<FieldValue> fieldsValues = fieldRepository.findByIdFieldSchemaIn(referenced);
        fieldsValues.stream().forEach(fieldValuePkOtherTable -> {
          if (fieldValue.getValue().equalsIgnoreCase(fieldValuePkOtherTable.getValue())) {
            fieldValuePkOtherTable.setValue(fieldValueVO.getValue());
            fieldRepository.saveValue(fieldValuePkOtherTable.getId(),
                fieldValuePkOtherTable.getValue());

          }
        });
        // we took the list of single pams value
        String idListOfSinglePamsField = null;
        for (Object document : fieldSchemasList) {
          if (((Document) document).get(HEADER_NAME) != null
              && ((Document) document).getString(HEADER_NAME).equals("ListOfSinglePams")) {
            idListOfSinglePamsField = ((Document) document).get("_id").toString();
            break;
          }
        }
        // we call pams service
        paMService.updateGroups(idListOfSinglePamsField, fieldValue,
            fieldNoValidationMapper.classToEntity(fieldValueVO));
      }
    }
  }

  /**
   * Gets the schema if reportable.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   *
   * @return the schema if reportable
   */
  @Override
  public DataSetSchema getSchemaIfReportable(Long datasetId, String tableSchemaId) {

    DataSetMetabase dataset = dataSetMetabaseRepository.findById(datasetId).orElse(null);
    DataSetSchema schema = null;
    if (dataset != null) {
      DatasetTypeEnum datasetType = getDatasetType(datasetId);
      TypeStatusEnum dataflowStatus =
          dataflowControllerZuul.getMetabaseById(dataset.getDataflowId()).getStatus();

      switch (datasetType) {
        case DESIGN:
          if (TypeStatusEnum.DESIGN.equals(dataflowStatus)) {
            schema =
                schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
          }
          break;
        case TEST:
        case REPORTING:
          if (TypeStatusEnum.DRAFT.equals(dataflowStatus)) {
            schema = filterSchemaByTable(tableSchemaId, dataset);
          }
          break;
        case REFERENCE:
          if (Boolean.TRUE.equals(referenceDatasetRepository.findById(datasetId)
              .orElse(new ReferenceDataset()).getUpdatable())) {
            schema =
                schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
          }
          break;
        default:
          break;
      }
    }
    return schema;
  }

  /**
   * Filter schema by table.
   *
   * @param tableSchemaId the table schema id
   * @param dataset the dataset
   * @return the data set schema
   */
  private DataSetSchema filterSchemaByTable(String tableSchemaId, DataSetMetabase dataset) {
    DataSetSchema schema;
    schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
    if (null != tableSchemaId) {
      TableSchema tableSchema = schema.getTableSchemas().stream()
          .filter(t -> tableSchemaId.equals(t.getIdTableSchema().toString())).findFirst()
          .orElse(null);
      if (null == tableSchema || Boolean.TRUE.equals(tableSchema.getReadOnly())
          || Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        schema = null;
      }
    }
    return schema;
  }


  /**
   * Save public files.
   *
   * @param dataflowId the dataflow id
   * @param dataSetDataProvider the data set data provider
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void savePublicFiles(Long dataflowId, Long dataSetDataProvider) throws IOException {

    LOG.info("Start creating files. DataflowId: {} DataProviderId: {}", dataflowId,
        dataSetDataProvider);

    List<RepresentativeVO> representativeList =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);

    // we find representative
    RepresentativeVO representative = representativeList.stream().filter(
        representativeData -> representativeData.getDataProviderId().equals(dataSetDataProvider))
        .findAny().orElse(null);

    if (null != representative) {
      // we create the dataflow folder to save it

      File directoryDataflow = new File(pathPublicFile, "dataflow-" + dataflowId);
      File directoryDataProvider =
          new File(directoryDataflow, "dataProvider-" + representative.getDataProviderId());
      // we create the dataprovider folder to save it andwe always delete it and put new files
      FileUtils.deleteDirectory(directoryDataProvider);

      if (!representative.isRestrictFromPublic()) {
        // we check if the representative have permit to do it
        createAllDatasetFiles(dataflowId, representative.getDataProviderId());
      } else {
        // we delete all file names in the table dataset
        List<DataSetMetabase> datasetMetabaseList = dataSetMetabaseRepository
            .findByDataflowIdAndDataProviderId(dataflowId, representative.getDataProviderId());
        datasetMetabaseList.stream().forEach(datasetFileName -> {
          datasetFileName.setPublicFileName(null);
        });
        dataSetMetabaseRepository.saveAll(datasetMetabaseList);
      }
    }
  }


  /**
   * Export public file.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param fileName the file name
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  public File exportPublicFile(Long dataflowId, Long dataProviderId, String fileName)
      throws IOException, EEAException {
    // we compound the route and create the file
    File file = null;
    if (dataProviderId != null) {
      file = new File(new File(new File(pathPublicFile, "dataflow-" + dataflowId.toString()),
          "dataProvider-" + dataProviderId.toString()), fileName);
    } else {
      file = new File(new File(pathPublicFile, "dataflow-" + dataflowId), fileName);
    }
    if (!file.exists()) {
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    }
    return file;
  }



  /**
   * Download exported file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  public File downloadExportedFile(Long datasetId, String fileName)
      throws IOException, EEAException {
    // we compound the route and create the file
    File file = new File(new File(pathPublicFile, "dataset-" + datasetId), fileName);
    if (!file.exists()) {
      LOG_ERROR.error(
          "Trying to download a file generated during the export dataset data process but the file is not found");
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    }
    return file;
  }



  /**
   * Creeate all dataset files.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createAllDatasetFiles(Long dataflowId, Long dataProviderId) throws IOException {

    DataProviderVO dataProvider = representativeControllerZuul.findDataProviderById(dataProviderId);

    List<DataSetMetabase> datasetMetabaseList =
        dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(dataflowId, dataProviderId);

    // now we create all files depends if they are avaliable
    for (DataSetMetabase datasetToFile : datasetMetabaseList) {
      if (schemasRepository
          .findAvailableInPublicByIdDataSetSchema(new ObjectId(datasetToFile.getDatasetSchema()))) {

        // we put the good in the correct field
        List<DesignDataset> desingDataset = designDatasetRepository.findByDataflowId(dataflowId);
        // we find the name of the dataset to asing it for file
        String datasetDesingName = "";
        for (DesignDataset designDatasetVO : desingDataset) {
          if (designDatasetVO.getDatasetSchema()
              .equalsIgnoreCase(datasetToFile.getDatasetSchema())) {
            datasetDesingName = designDatasetVO.getDataSetName();
          }
        }

        try {
          // 1º we create
          byte[] file = exportFile(datasetToFile.getId(), FileTypeEnum.XLSX.getValue(), null);
          // we save the file in its files
          if (null != file) {
            String nameFileUnique = String.format(FILE_PUBLIC_DATASET_PATTERN_NAME,
                dataProvider.getCode(), datasetDesingName);
            String nameFileScape = nameFileUnique + ".xlsx";

            // we create the files and zip with the document if it is necessary
            createFilesAndZip(dataflowId, dataProviderId, datasetToFile, file, nameFileUnique,
                nameFileScape);


            // we save the file in metabase with the name without the route
            datasetToFile.setPublicFileName(nameFileUnique + ".zip");
            dataSetMetabaseRepository.save(datasetToFile);
          }
        } catch (EEAException e) {
          LOG_ERROR.error(
              "File not created in dataflow {} with dataprovider {} with datasetId {} message {}",
              dataflowId, datasetToFile.getDataProviderId(), datasetToFile.getId(), e.getMessage(),
              e);
        }
        LOG.info("Start files created in DataflowId: {} with DataProviderId: {}", dataflowId,
            datasetToFile.getDataProviderId());
      } else {
        datasetToFile.setPublicFileName(null);
        dataSetMetabaseRepository.save(datasetToFile);
      }
    }
  }

  /**
   * Creates the files and zip.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param datasetToFile the dataset to file
   * @param file the file
   * @param nameFileUnique the name file unique
   * @param nameFileScape the name file scape
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createFilesAndZip(Long dataflowId, Long dataProviderId,
      DataSetMetabase datasetToFile, byte[] file, String nameFileUnique, String nameFileScape)
      throws IOException {

    // we create folder to save the file.zip
    File fileFolderProvider = null;
    if (dataProviderId != null) {
      fileFolderProvider = new File((new File(pathPublicFile, "dataflow-" + dataflowId.toString())),
          "dataProvider-" + dataProviderId.toString());
    } else {
      fileFolderProvider = new File(pathPublicFile, "dataflow-" + dataflowId.toString());
    }
    fileFolderProvider.mkdirs();

    // we create the file.zip
    File fileWriteZip = null;
    if (dataProviderId != null) {
      fileWriteZip =
          new File(new File(new File(pathPublicFile, "dataflow-" + dataflowId.toString()),
              "dataProvider-" + dataProviderId.toString()), nameFileUnique + ".zip");
    } else {
      fileWriteZip = new File(new File(pathPublicFile, "dataflow-" + dataflowId.toString()),
          nameFileUnique + ".zip");
    }
    // create the context to add all files in a treemap inside to attachment and file information
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileWriteZip.toString()))) {
      // we get the dataschema and check every table to see if find any field attachemnt
      DataSetSchema dataSetSchema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(datasetToFile.getDatasetSchema()));
      for (TableSchema tableSchema : dataSetSchema.getTableSchemas()) {

        // we find if in any table have one field type ATTACHMENT
        List<FieldSchema> fieldSchemaAttachment = tableSchema.getRecordSchema().getFieldSchema()
            .stream().filter(field -> DataType.ATTACHMENT.equals(field.getType()))
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(fieldSchemaAttachment)) {

          LOG.info("We  are in tableSchema with id {} looking if we have attachments",
              tableSchema.getIdTableSchema());
          // We took every field for every table
          for (FieldSchema fieldAttach : fieldSchemaAttachment) {
            List<AttachmentValue> attachmentValue = attachmentRepository
                .findAllByIdFieldSchemaAndValueIsNotNull(fieldAttach.getIdFieldSchema().toString());

            // if there are filled we create a folder and inside of any folder we create the fields
            if (!CollectionUtils.isEmpty(attachmentValue)) {
              LOG.info(
                  "We  are in tableSchema with id {}, checking field {} and we have attachments files",
                  tableSchema.getIdTableSchema(), fieldAttach.getIdFieldSchema());

              for (AttachmentValue attachment : attachmentValue) {
                try {
                  ZipEntry eFieldAttach = new ZipEntry(
                      tableSchema.getNameTableSchema() + "/" + attachment.getFileName());
                  out.putNextEntry(eFieldAttach);
                  out.write(attachment.getContent(), 0, attachment.getContent().length);
                } catch (ZipException e) {
                  LOG.info("Error creating file {} because already exist", attachment.getFileName(),
                      e);
                }
                out.closeEntry();
              }
            }
          }
        }
      }

      ZipEntry e = new ZipEntry(nameFileScape);
      out.putNextEntry(e);
      out.write(file, 0, file.length);
      out.closeEntry();
      LOG.info("We create file {} in the route ", fileWriteZip);
    }
  }


  /**
   * Gets the table schema.
   *
   * @param tableSchemaId the table schema id
   * @param datasetSchemaId the dataset schema id
   * @return the table schema
   */
  private TableSchema getTableSchema(String tableSchemaId, String datasetSchemaId) {

    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    TableSchema tableSchema = null;

    if (null != datasetSchema && null != datasetSchema.getTableSchemas()
        && ObjectId.isValid(tableSchemaId)) {
      ObjectId oid = new ObjectId(tableSchemaId);
      tableSchema = datasetSchema.getTableSchemas().stream()
          .filter(ts -> oid.equals(ts.getIdTableSchema())).findFirst().orElse(null);
    }

    return tableSchema;
  }


  /**
   * Check any schema available in public.
   *
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Override
  public boolean checkAnySchemaAvailableInPublic(Long dataflowId) {

    List<DataSetMetabase> dataSetMetabaseList =
        dataSetMetabaseRepository.findByDataflowIdAndProviderIdNotNull(dataflowId);

    boolean anySchemaAvailableInPublic = false;

    for (DataSetMetabase dataset : dataSetMetabaseList) {
      anySchemaAvailableInPublic = schemasRepository
          .findAvailableInPublicByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));

      if (anySchemaAvailableInPublic) {
        break;
      }
    }

    return anySchemaAvailableInPublic;
  }


  /**
   * Delete records from id table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  private void deleteRecordsFromIdTableSchema(Long datasetId, String tableSchemaId) {
    recordRepository.deleteRecordWithIdTableSchema(tableSchemaId);
    LOG.info("Executed deleteRecords: datasetId={}, tableSchemaId={}", datasetId, tableSchemaId);
  }

  /**
   * Initialize dataset.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  public void initializeDataset(Long datasetId, String idDatasetSchema) {
    try {
      // 1.Insert the dataset schema into DatasetValue
      DatasetValue dataset = new DatasetValue();
      dataset.setIdDatasetSchema(idDatasetSchema);
      dataset.setId(datasetId);

      // 2.Search the table schemas of the dataset and then insert it into TableValue
      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));
      List<TableValue> tableValues = new ArrayList<>();
      for (TableSchema tableSchema : schema.getTableSchemas()) {
        TableValue tv = new TableValue();
        tv.setIdTableSchema(tableSchema.getIdTableSchema().toString());
        tv.setDatasetId(dataset);
        tableValues.add(tv);
      }
      dataset.setTableValues(tableValues);
      TenantResolver.setTenantName(String.format(DATASET_ID, datasetId));
      datasetRepository.save(dataset);


      List<Statistics> statsList = Collections.synchronizedList(new ArrayList<>());

      DataSetMetabase datasetMb =
          dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());

      Map<String, String> mapIdNameDatasetSchema = new HashMap<>();
      for (TableSchema tableSchema : schema.getTableSchemas()) {
        mapIdNameDatasetSchema.put(tableSchema.getIdTableSchema().toString(),
            tableSchema.getNameTableSchema());
      }

      tableValues.parallelStream().forEach(tableValue -> statsList.addAll(
          initializeTableStats(tableValue.getIdTableSchema(), datasetId, mapIdNameDatasetSchema)));

      statsList.add(fillStat(datasetId, null, "idDataSetSchema", idDatasetSchema));

      statsList.add(fillStat(datasetId, null, "nameDataSetSchema", datasetMb.getDataSetName()));

      statsList.add(fillStat(datasetId, null, "datasetErrors", "false"));

      TenantResolver.setTenantName(String.format(DATASET_ID, datasetId));
      statisticsRepository.saveAll(statsList);

      LOG.info("Statistics save to datasetId {}.", datasetId);
      DatasetTypeEnum type = getDatasetType(datasetId);
      if (DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
          || DatasetTypeEnum.REFERENCE.equals(type)) {
        DesignDataset originDatasetDesign =
            designDatasetRepository.findFirstByDatasetSchema(idDatasetSchema).orElse(null);
        if (null != originDatasetDesign) {
          // and we have found the design dataset
          // with data to be copied into the
          // target dataset
          spreadDataPrefill(schema, originDatasetDesign.getId(), datasetMb);

          // create zip to the reference
          if (DatasetTypeEnum.REFERENCE.equals(type)) {
            createReferenceDatasetFiles(datasetMb);
          }

        }
      }
    } catch (Exception e) {
      LOG_ERROR.error(
          "Error executing the processes after creating a new empty dataset. Error message: {}",
          e.getMessage(), e);
    }
  }

  /**
   * Initialize table stats.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   * @param mapIdNameDatasetSchema the map id name dataset schema
   * @return the list
   */
  private List<Statistics> initializeTableStats(final String tableSchemaId, final Long datasetId,
      final Map<String, String> mapIdNameDatasetSchema) {
    List<Statistics> stats = new ArrayList<>();

    stats.add(fillStat(datasetId, tableSchemaId, "idTableSchema", tableSchemaId));
    stats.add(fillStat(datasetId, tableSchemaId, "nameTableSchema",
        mapIdNameDatasetSchema.get(tableSchemaId)));
    stats.add(fillStat(datasetId, tableSchemaId, "totalErrors", "0"));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecords", "0"));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithBlockers", "0"));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithErrors", "0"));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithWarnings", "0"));
    stats.add(fillStat(datasetId, tableSchemaId, "totalRecordsWithInfos", "0"));

    Statistics statsTableErrors = new Statistics();
    statsTableErrors.setIdTableSchema(tableSchemaId);
    statsTableErrors.setStatName("tableErrors");
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(datasetId);
    statsTableErrors.setDataset(reporting);
    statsTableErrors.setValue("false");

    stats.add(statsTableErrors);

    return stats;
  }

  /**
   * Export dataset ETLSQL.
   *
   * @param datasetId the dataset id
   * @param outputStream the output stream
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @throws EEAException the EEA exception
   */
  private void exportDatasetETLSQL(Long datasetId, OutputStream outputStream, String tableSchemaId,
      Integer limit, Integer offset, String filterValue, String columnName) throws EEAException {
    try {
      // Delete the query log and the timestamp part later, once the tests are finished.
      outputStream.write(recordRepository.findAndGenerateETLJson(datasetId, outputStream,
          tableSchemaId, limit, offset, filterValue, columnName).getBytes());
      LOG.info("Finish ETL Export proccess for Dataset:{}", datasetId);
    } catch (IOException e) {
      LOG.error("ETLExport error in  Dataset: {}", datasetId, e);
    }
  }

  /**
   * Creates the reference dataset files.
   *
   * @param dataset the dataset
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createReferenceDatasetFiles(DataSetMetabase dataset) throws IOException {

    List<DesignDataset> desingDataset =
        designDatasetRepository.findByDataflowId(dataset.getDataflowId());
    // look for the name of the design dataset to put the right name to the file
    String datasetDesingName = "";
    for (DesignDataset designDatasetVO : desingDataset) {
      if (designDatasetVO.getDatasetSchema().equalsIgnoreCase(dataset.getDatasetSchema())) {
        datasetDesingName = designDatasetVO.getDataSetName();
      }
    }

    try {
      // create the excel file
      byte[] file = exportFile(dataset.getId(), FileTypeEnum.XLSX.getValue(), null);
      // we save the file in its files
      if (null != file) {
        String nameFileUnique = String.format("%s", datasetDesingName);
        String nameFileScape = nameFileUnique + ".xlsx";

        // we create the files and zip with the attachment if it is necessary
        createFilesAndZip(dataset.getDataflowId(), null, dataset, file, nameFileUnique,
            nameFileScape);

        // we save the file in metabase with the name without the route
        dataset.setPublicFileName(nameFileUnique + ".zip");
        dataSetMetabaseRepository.save(dataset);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("File not created in dataflow {}. Message: {}", dataset.getDataflowId(),
          e.getMessage(), e);
    }
    LOG.info("File created in dataflowId {}", dataset.getDataflowId());

  }

  /**
   * Store records.
   *
   * @param datasetId the dataset id
   * @param recordList the record list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @Override
  public void storeRecords(@DatasetId Long datasetId, List<RecordValue> recordList)
      throws IOException, SQLException {

    String schema = LiteralConstants.DATASET_PREFIX + datasetId;
    LOG.info("RN3-Import - Getting connections: datasetId={}", datasetId);
    ConnectionDataVO connectionDataVO = recordStoreControllerZuul.getConnectionToDataset(schema);

    LOG.info("RN3-Import - Starting PostgresBulkImporter: datasetId={}", datasetId);
    try (
        PostgresBulkImporter recordsImporter = new PostgresBulkImporter(connectionDataVO, schema,
            "record_value (ID, ID_RECORD_SCHEMA,ID_TABLE,DATASET_PARTITION_ID,DATA_PROVIDER_CODE) ",
            importPath);
        PostgresBulkImporter fieldsImporter = new PostgresBulkImporter(connectionDataVO, schema,
            "field_value (ID, TYPE, VALUE, ID_FIELD_SCHEMA, ID_RECORD, GEOMETRY) ", importPath)) {

      LOG.info("RN3-Import - PostgresBulkImporter started: datasetId={}", datasetId);

      for (RecordValue recordValue : recordList) {

        String recordId = (String) recordValueIdGenerator.generate(null, recordValue);
        recordsImporter.addTuple(new Object[] {recordId, recordValue.getIdRecordSchema(),
            recordValue.getTableValue().getId(), recordValue.getDatasetPartitionId(),
            recordValue.getDataProviderCode()});

        for (FieldValue fieldValue : recordValue.getFields()) {
          String fieldId = (String) fieldValueIdGenerator.generate(null, fieldValue);
          fieldsImporter.addTuple(new Object[] {fieldId, fieldValue.getType().getValue(),
              fieldValue.getValue(), fieldValue.getIdFieldSchema(), recordId, null});
        }
      }

      LOG.info("RN3-Import file: Temporary binary files CREATED for datasetId={}", datasetId);
      recordsImporter.copy();
      fieldsImporter.copy();
      LOG.info("RN3-Import file: Temporary binary files IMPORTED for datasetId={}", datasetId);
    } catch (SQLException e) {
      LOG_ERROR.error("Cannot save the records for dataset {}", datasetId, e);
      throw e;
    }
  }

  /**
   * Update records with conditions.
   *
   * @param recordList the record list
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @Override
  @Transactional
  public void updateRecordsWithConditions(List<RecordValue> recordList, Long datasetId,
      TableSchema tableSchema) {
    LOG.info("Import dataset table {} with conditions", tableSchema.getNameTableSchema());
    boolean readOnly =
        tableSchema.getRecordSchema().getFieldSchema().stream().anyMatch(FieldSchema::getReadOnly);
    tableRepository.countRecordsByIdTableSchema(tableSchema.getIdTableSchema().toString());

    // get list paginated of old records to modify
    TableValue targetTable =
        tableRepository.findByIdTableSchema(tableSchema.getIdTableSchema().toString());
    List<RecordValue> oldRecords =
        recordRepository.findOrderedNativeRecord(targetTable.getId(), datasetId, null);
    // sublist records to insert
    List<RecordValue> recordsToSave = new ArrayList<>();

    if (!readOnly) {
      Iterator<RecordValue> itr = recordList.iterator();
      for (RecordValue oldRecord : oldRecords) {
        if (itr.hasNext()) {
          refillFields(oldRecord, itr.next().getFields());
        } else {
          refillFields(oldRecord, null);
        }
        oldRecord.setTableValue(targetTable);
        recordsToSave.add(oldRecord);
      }
    } else {
      List<ObjectId> readOnlyFields =
          tableSchema.getRecordSchema().getFieldSchema().stream().filter(FieldSchema::getReadOnly)
              .map(FieldSchema::getIdFieldSchema).collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(oldRecords)
          && readOnlyFields.size() != tableSchema.getRecordSchema().getFieldSchema().size()) {
        for (RecordValue oldRecord : oldRecords) {
          Map<Integer, Integer> mapPosition =
              mapPositionReadOnlyFieldsForReference(readOnlyFields, oldRecord, recordList.get(0));
          findByReadOnlyRecords(mapPosition, oldRecord, recordList);
          oldRecord.setTableValue(targetTable);
          recordsToSave.add(oldRecord);
        }
      }
    }
    LOG.info("Import dataset table {} with {} number of records", tableSchema.getNameTableSchema(),
        recordsToSave.size());

    saveAllRecords(datasetId, recordsToSave);
  }


  /**
   * Refill fields.
   *
   * @param oldRecord the old record
   * @param fieldValues the field values
   */
  private void refillFields(RecordValue oldRecord, List<FieldValue> fieldValues) {
    if (fieldValues != null) {
      oldRecord.getFields().stream().forEach(oldField -> {
        oldField.setValue(fieldValues.stream()
            .filter(field -> oldField.getIdFieldSchema().equals(field.getIdFieldSchema()))
            .map(FieldValue::getValue).findFirst().orElse(""));
        oldField.setRecord(oldRecord);
      });
    } else {
      oldRecord.getFields().forEach(field -> field.setValue(""));
    }
  }

  /**
   * Map position read only fields for reference.
   *
   * @param readOnlyFields the read only fields
   * @param recordValue the record value
   * @param newRecordValues the new record values
   * @return the map
   */
  private Map<Integer, Integer> mapPositionReadOnlyFieldsForReference(List<ObjectId> readOnlyFields,
      RecordValue recordValue, RecordValue newRecordValues) {
    Map<Integer, Integer> mapPosition = new HashMap<>();
    for (ObjectId id : readOnlyFields) {
      mapPosition.put(
          recordValue.getFields().stream().map(FieldValue::getIdFieldSchema)
              .collect(Collectors.toList()).indexOf(id.toString()),
          newRecordValues.getFields().stream().map(FieldValue::getIdFieldSchema)
              .collect(Collectors.toList()).indexOf(id.toString()));
    }
    return mapPosition;
  }

  /**
   * Find by read only records.
   *
   * @param readOnlyPositionFields the read only position fields
   * @param oldRecord the old record
   * @param recordList the record list
   * @return the record value
   */
  private void findByReadOnlyRecords(Map<Integer, Integer> readOnlyPositionFields,
      RecordValue oldRecord, List<RecordValue> recordList) {

    RecordValue recordToUpdate = recordList.stream()
        .filter(record -> readOnlyPositionFields.entrySet().stream()
            .allMatch(entry -> record.getFields().get(entry.getValue()).getValue()
                .equals(oldRecord.getFields().get(entry.getKey()).getValue())))
        .findFirst().orElse(null);
    if (recordToUpdate != null) {
      refillFields(oldRecord, recordToUpdate.getFields());
    }

  }

}
