package org.eea.dataset.service.impl;

import org.bson.Document;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.DremioRecordMapper;
import org.eea.dataset.mapper.DremioValidationMapper;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DataLakeDataRetrieverService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.validation.DremioValidationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum.*;
import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DataLakeDataRetrieverServiceImpl implements DataLakeDataRetrieverService {

    private DatasetMetabaseService datasetMetabaseService;
    private JdbcTemplate dremioJdbcTemplate;
    private FileCommonUtils fileCommon;
    private S3Service s3Service;
    private SchemasRepository schemasRepository;
    private S3Client s3Client;
    private S3Helper s3Helper;
    private DremioHelperService dremioHelperService;
    private DatasetSchemaService datasetSchemaService;

    @Autowired
    public DataLakeDataRetrieverServiceImpl(DatasetMetabaseService datasetMetabaseService, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate,
                                            FileCommonUtils fileCommon, S3Service s3Service, SchemasRepository schemasRepository, S3Client s3Client, S3Helper s3Helper,
                                            DremioHelperService dremioHelperService, DatasetSchemaService datasetSchemaService) {
        this.datasetMetabaseService = datasetMetabaseService;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.fileCommon = fileCommon;
        this.s3Service = s3Service;
        this.schemasRepository = schemasRepository;
        this.s3Client = s3Client;
        this.s3Helper = s3Helper;
        this.dremioHelperService = dremioHelperService;
        this.datasetSchemaService = datasetSchemaService;
    }

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DataLakeDataRetrieverServiceImpl.class);
    private static final int MAX_FILTERS = 5;

    @Override
    public TableVO getTableValuesDLById(final Long datasetId, final String idTableSchema, Pageable pageable, final String fields, ErrorTypeEnum[] levelError,
                                        String[] qcCodes, String fieldSchema, String fieldValue) throws EEAException {
        TableVO result;
        DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
        String datasetSchemaId = dataset.getDatasetSchema();
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(idTableSchema, datasetSchemaId);
        if (COLLECTION.equals(dataset.getDatasetTypeEnum())) {
            result = getDCTableResult(dataset, tableSchemaVO, pageable, fields, fieldValue, levelError, qcCodes);
        } else if (EUDATASET.equals(dataset.getDatasetTypeEnum())) {
            result = getEUDatasetTableResult(dataset, tableSchemaVO, pageable, fields, fieldValue, levelError, qcCodes);
        } else {
            result = getDatasetTableResult(dataset, tableSchemaVO, pageable, fields, fieldValue, levelError, qcCodes);
        }
        return result;
    }

    /**
     * Finds dataset total records
     * @param dataset
     * @param tableSchemaVO
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param dataset
     * @param tableSchemaVO
     * @param levelError
     * @param qcCodes
     * @return
     */
    private TableVO getDatasetTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) {
        Long totalRecords = 0L;
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();
        S3PathResolver s3PathResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema());
        boolean folderExist = s3Helper.checkFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH) : {}", folderExist);
        if (folderExist && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
            StringBuilder dataQuery = new StringBuilder();
            StringBuilder recordsCountQuery = new StringBuilder();
            if (REFERENCE.equals(dataset.getDatasetTypeEnum())) {
                s3PathResolver.setPath(S3_DATAFLOW_REFERENCE_QUERY_PATH);
                totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.getRecordsCountQuery(s3PathResolver), Long.class);
                dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver) + " t ");
                recordsCountQuery.append("select count(record_id) from " + s3Service.getTableAsFolderQueryPath(s3PathResolver) + " t ");
            } else {
                totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class);
                dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH) + " t ");
                recordsCountQuery.append("select count(record_id) from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH) + " t ");
            }
            result.setTotalRecords(totalRecords);

            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            s3PathResolver.setTableName(S3_VALIDATION);
            String validationTablePath = s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
            StringBuilder filteredQuery = buildFilteredQuery(dataset, fields, fieldValue, fieldIdMap, levelError, qcCodes, validationTablePath);
            if (filteredQuery.toString().isEmpty() && levelError!=null && levelError.length==0) {
                result.setTotalFilteredRecords(0L);
                result.setTotalRecords(totalRecords);
                result.setRecords(new ArrayList<>());
            } else {
                recordsCountQuery.append(filteredQuery);
                getTableResultsAndValidations(dataset, tableSchemaVO, pageable, result, s3PathResolver, dataQuery, recordsCountQuery, validationTablePath, filteredQuery);
            }
        } else {
            setEmptyResults(result);
        }
        return result;
    }

    /**
     * Gets table results and validations
     * @param dataset
     * @param tableSchemaVO
     * @param pageable
     * @param result
     * @param s3PathResolver
     * @param dataQuery
     * @param recordsCountQuery
     * @param validationTablePath
     * @param filteredQuery
     */
    private void getTableResultsAndValidations(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, TableVO result, S3PathResolver s3PathResolver, StringBuilder dataQuery, StringBuilder recordsCountQuery, String validationTablePath, StringBuilder filteredQuery) {
        Long totalFilteredRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery.toString(), Long.class);
        result.setTotalFilteredRecords(totalFilteredRecords);

        pageable = calculatePageable(pageable, totalFilteredRecords);
        //pagination
        if (pageable !=null) {
            buildPaginationQuery(pageable, filteredQuery);
        }
        dataQuery.append(filteredQuery);
        LOG.info("For datasetId {} dataQuery.toString() : {}", dataset.getId(), dataQuery);
        List<RecordVO> recordVOS = getRecordVOS(dataset.getDatasetSchema(), tableSchemaVO, dataQuery);
        result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
        result.setRecords(recordVOS);

        if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH)) {
            if (!dremioHelperService.checkFolderPromoted(s3PathResolver, S3_VALIDATION)) {
                dremioHelperService.promoteFolderOrFile(s3PathResolver, S3_VALIDATION);
            }
            if (recordVOS.size()>0) {
                retrieveValidations(recordVOS, tableSchemaVO.getNameTableSchema(), validationTablePath);
            }
        }
    }

    /**
     * Finds dataCollection total records
     * @param dataset
     * @param tableSchemaVO
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param dataset
     * @param tableSchemaVO
     * @param levelError
     * @param qcCodes
     * @return
     */
    private TableVO getDCTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) {
        Long totalRecords = 0L;
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();
        S3PathResolver s3PathResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema());
        boolean folderExist = s3Helper.checkTableNameDCFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_DC_FOLDER_PATH) : {}", folderExist);
        if (folderExist && dremioHelperService.checkFolderPromoted(s3PathResolver,s3PathResolver.getTableName())) {
            s3PathResolver.setPath(S3_TABLE_NAME_DC_FOLDER_PATH);
            totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQueryDC(s3PathResolver), Long.class);
            result.setTotalRecords(totalRecords);
            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            pageable = calculatePageable(pageable, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            FieldSchemaVO fieldSchemaProviderCode = new FieldSchemaVO();
            fieldSchemaProviderCode.setName("data_provider_code");
            fieldIdMap.put("data_provider_code", fieldSchemaProviderCode);
            StringBuilder filteredQuery = buildFilteredQuery(dataset, fields, fieldValue, fieldIdMap, levelError, qcCodes, null);
            StringBuilder recordsCountQuery = new StringBuilder();
            recordsCountQuery.append("select count(record_id) from " + s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH) + " t ").append(filteredQuery);
            Long totalFilteredRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery.toString(), Long.class);
            result.setTotalFilteredRecords(totalFilteredRecords);

            pageable = calculatePageable(pageable, totalFilteredRecords);
            //pagination
            if (pageable !=null) {
                buildPaginationQuery(pageable, filteredQuery);
            }

            StringBuilder dataQuery = new StringBuilder();
            dataQuery.append("select * from " + s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH) + " t ");
            dataQuery.append(filteredQuery);
            LOG.info("For datasetId {} dataQuery.toString() : {}", dataset.getId(), dataQuery);
            List<RecordVO> recordVOS = getRecordVOS(dataset.getDatasetSchema(), tableSchemaVO, dataQuery);
            result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
            result.setRecords(recordVOS);
        } else {
            setEmptyResults(result);
        }
        return result;
    }

    /**
     * Finds EU dataset total records
     * @param dataset
     * @param tableSchemaVO
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param dataset
     * @param tableSchemaVO
     * @param levelError
     * @param qcCodes
     * @return
     */
    private TableVO getEUDatasetTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) {
        Long totalRecords = 0L;
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();
        S3PathResolver s3PathResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema());
        boolean folderExist = s3Helper.checkTableNameDCFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_DC_FOLDER_PATH) : {}", folderExist);
        if (folderExist && dremioHelperService.checkFolderPromoted(s3PathResolver,s3PathResolver.getTableName())) {
            s3PathResolver.setPath(S3_TABLE_NAME_EU_QUERY_PATH);
            totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.getRecordsCountQuery(s3PathResolver), Long.class);
            result.setTotalRecords(totalRecords);
            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            FieldSchemaVO fieldSchemaProviderCode = new FieldSchemaVO();
            fieldSchemaProviderCode.setName("data_provider_code");
            fieldIdMap.put("data_provider_code", fieldSchemaProviderCode);
            StringBuilder filteredQuery = buildFilteredQuery(dataset, fields, fieldValue, fieldIdMap, levelError, qcCodes, null);
            StringBuilder recordsCountQuery = new StringBuilder();
            recordsCountQuery.append("select count(record_id) from " + s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_EU_QUERY_PATH) + " t ").append(filteredQuery);
            Long totalFilteredRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery.toString(), Long.class);
            result.setTotalFilteredRecords(totalFilteredRecords);

            pageable = calculatePageable(pageable, totalFilteredRecords);
            //pagination
            if (pageable !=null) {
                buildPaginationQuery(pageable, filteredQuery);
            }

            StringBuilder dataQuery = new StringBuilder();
            dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver) + " t ");
            dataQuery.append(filteredQuery);
            LOG.info("For datasetId {} dataQuery.toString() : {}", dataset.getId(), dataQuery);
            List<RecordVO> recordVOS = getRecordVOS(dataset.getDatasetSchema(), tableSchemaVO, dataQuery);
            result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
            result.setRecords(recordVOS);
        } else {
            setEmptyResults(result);
        }
        return result;
    }

    /**
     * Get records
     * @param datasetSchema
     * @param tableSchemaVO
     * @param dataQuery
     * @return
     */
    private List<RecordVO> getRecordVOS(String datasetSchema , TableSchemaVO tableSchemaVO, StringBuilder dataQuery) {
        DremioRecordMapper recordMapper = new DremioRecordMapper();
        recordMapper.setRecordSchemaVO(tableSchemaVO.getRecordSchema()).setDatasetSchemaId(datasetSchema).setTableSchemaId(tableSchemaVO.getIdTableSchema());
        List<RecordVO> recordVOS = dremioJdbcTemplate.query(dataQuery.toString(), recordMapper);
        return recordVOS;
    }

    /**
     * Sets empty results
     * @param result
     */
    private static void setEmptyResults(TableVO result) {
        result.setTotalFilteredRecords(0L);
        result.setTableValidations(new ArrayList<>());
        result.setTotalRecords(0L);
        result.setRecords(new ArrayList<>());
    }

    /**
     * Builds queries and returns data records
     * @param dataset
     * @param fields
     * @param fieldValue
     * @param fieldIdMap
     * @param levelError
     * @param qcCodes
     * @param validationTablePath
     * @return
     */
    @Override
    public StringBuilder buildFilteredQuery(DataSetMetabaseVO dataset, String fields, String fieldValue, Map<String, FieldSchemaVO> fieldIdMap,
                                      ErrorTypeEnum[] levelError, String[] qcCodes, String validationTablePath) {
        StringBuilder query = new StringBuilder();
        boolean levelErrorNotEmpty = levelError!=null && levelError.length>0 && levelError.length!=MAX_FILTERS;
        boolean qcCodesNotEmpty = qcCodes!=null && qcCodes.length>0;
        //filter value
        if (!fieldValue.equals("")) {
            buildFieldValueFilterQuery(fieldValue, fieldIdMap, query);
        }
        //filter by levelError
        if (levelErrorNotEmpty && validationTablePath!=null) {
            buildLevelErrorQueryFilter(fieldValue, query, levelError, validationTablePath);
        }
        //filter by qc_code
        if (qcCodesNotEmpty && validationTablePath!=null) {
            buildQcCodeFilterQuery(fieldValue, query, levelErrorNotEmpty, qcCodes, validationTablePath);
        }
        //sorting
        if (fields !=null) {
            buildSortQuery(fields, dataset.getDatasetSchema(), fieldIdMap, query);
        }
        return query;
    }

    /**
     * Builds query for filtering records by qcCodes
     * @param fieldValue
     * @param dataQuery
     * @param levelErrorNotEmpty
     * @param qcCodes
     * @param validationTablePath
     */
    private static void buildQcCodeFilterQuery(String fieldValue, StringBuilder dataQuery, boolean levelErrorNotEmpty, String[] qcCodes, String validationTablePath) {
        List<String> qcCodesList = Arrays.asList(qcCodes);
        String qcCodesValues = qcCodesList.stream() .map(s -> "\'" + s + "\'") .collect(Collectors.joining(", "));
        if (!fieldValue.equals("") || levelErrorNotEmpty) {
            dataQuery.append(" AND ");
        } else {
            dataQuery.append(" WHERE ");
        }
        dataQuery.append("(EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v WHERE t.RECORD_ID=v.RECORD_ID AND QC_CODE IN (").append(qcCodesValues).append(")))");
    }

    /**
     * Builds query for filtering records by level error
     * @param fieldValue
     * @param dataQuery
     * @param levelError
     * @param validationTablePath
     */
    private static void buildLevelErrorQueryFilter(String fieldValue, StringBuilder dataQuery, ErrorTypeEnum[] levelError, String validationTablePath) {
        List<ErrorTypeEnum> levelErrorList = Arrays.asList(levelError);
        String levelErrorValues = levelErrorList.stream().map(s -> "\'" + s + "\'").collect(Collectors.joining(", "));
        if (!fieldValue.equals("")) {
            dataQuery.append(" AND ");
        } else {
            dataQuery.append(" WHERE ");
        }
        if (levelErrorList.size() == 1 && levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
            dataQuery.append("(NOT EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v ").append(" WHERE t.RECORD_ID=v.RECORD_ID))");
        } else {
            dataQuery.append("(EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v WHERE t.RECORD_ID=v.RECORD_ID AND VALIDATION_LEVEL IN (").append(levelErrorValues).append("))");
            if (levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
                dataQuery.append(" OR NOT EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v ").append(" WHERE t.RECORD_ID=v.RECORD_ID)");
            }
            dataQuery.append(")");
        }
    }

    /**
     * retrieves validation results from dremio validation folder and maps them to fieldVO objects
     * @param recordVOS
     * @param tableName
     * @param validationTablePath
     */
    private void retrieveValidations(List<RecordVO> recordVOS, String tableName, String validationTablePath) {
        StringBuilder validationQuery = new StringBuilder();
        validationQuery.append("select * from " + validationTablePath);
        validationQuery.append(" where table_name='").append(tableName).append("'").append(" and record_id in ('");
        AtomicInteger count = new AtomicInteger();
        recordVOS.forEach(recordVO -> {
            if (count.get() != 0) {
                validationQuery.append(",'");
            }
            validationQuery.append(recordVO.getId()).append("'");
            if (count.get() == 0) {
                count.getAndIncrement();
            }
        });
        validationQuery.append(")");
        List<DremioValidationVO> dremioValidationsVOS = dremioJdbcTemplate.query(validationQuery.toString(), new DremioValidationMapper());
        for (DremioValidationVO dv : dremioValidationsVOS) {
            ValidationVO validationVO = getValidationVO(dv);
            List<RecordVO> records =  recordVOS.stream().filter(recordVO -> recordVO.getId().equals(dv.getRecordId())).collect(Collectors.toList());
            if (dv.getValidationArea().equals(EntityTypeEnum.FIELD) || dv.getValidationArea().equals(EntityTypeEnum.TABLE)) {
                records.forEach(vr -> vr.getFields().parallelStream().forEach(fieldVO -> {
                    if (fieldVO.getName().equals(dv.getFieldName())) {
                        setFieldValidations(validationVO, fieldVO);
                    }
                }));
            } else if (dv.getValidationArea().equals(EntityTypeEnum.RECORD)) {
                records.forEach(recordVO -> {
                    setRecordValidations(validationVO, recordVO);
                });
            }
        }
    }

    /**
     * Sets record validations
     * @param validationVO
     * @param recordVO
     */
    private static void setRecordValidations(ValidationVO validationVO, RecordVO recordVO) {
        List<RecordValidationVO> recordValidations = new ArrayList<>();
        RecordValidationVO recordValidationVO = new RecordValidationVO();
        recordValidationVO.setValidation(validationVO);
        if (recordVO.getRecordValidations()!=null) {
            recordValidations = recordVO.getRecordValidations();
        }
        recordValidations.add(recordValidationVO);
        recordVO.setRecordValidations(recordValidations);
    }

    /**
     * Sets field validations
     * @param validationVO
     * @param fieldVO
     */
    private static void setFieldValidations(ValidationVO validationVO, FieldVO fieldVO) {
        FieldValidationVO fieldValidationVO = new FieldValidationVO();
        List<FieldValidationVO> fieldValidations = new ArrayList<>();
        fieldValidationVO.setValidation(validationVO);
        if (fieldVO.getFieldValidations()!=null) {
            fieldValidations = fieldVO.getFieldValidations();
        }
        fieldValidations.add(fieldValidationVO);
        fieldVO.setFieldValidations(fieldValidations);
    }

    /**
     * Get validationVO
     * @param dv
     * @return
     */
    private static ValidationVO getValidationVO(DremioValidationVO dv) {
        ValidationVO validationVO = new ValidationVO();
        validationVO.setId(dv.getPk());
        validationVO.setLevelError(dv.getValidationLevel());
        validationVO.setMessage(dv.getMessage());
        validationVO.setTypeEntity(dv.getValidationArea());
        return validationVO;
    }

    /**
     * builds query according to pagination options
     * @param pageable
     * @param dataQuery
     */
    private static void buildPaginationQuery(Pageable pageable, StringBuilder dataQuery) {
        int limit;
        int offset;
        limit = pageable.getPageSize();
        offset = pageable.getPageNumber() * pageable.getPageSize();
        if (limit!=0) {
            dataQuery.append(" LIMIT ").append(limit);
        }
        if (offset!=0) {
            dataQuery.append(" OFFSET ").append(offset);
        }
    }

    /**
     * builds query according to sorting options
     * @param fields
     * @param datasetSchemaId
     * @param fieldIdMap
     * @param dataQuery
     */
    private void buildSortQuery(String fields, String datasetSchemaId, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        String[] sort = fields.split(":");
        String sortFieldId = sort[0];
        FieldSchemaVO sortField = fieldIdMap.get(sortFieldId);
        if (DataType.LINK.equals(sortField.getType())
                || DataType.EXTERNAL_LINK.equals(sortField.getType())) {
            Document documentField =
                    schemasRepository.findFieldSchema(datasetSchemaId, sortField.getName());
            Document documentReference = (Document) documentField.get("referencedField");
            Document documentFieldReferenced =
                    schemasRepository.findFieldSchema(documentReference.get("idDatasetSchema").toString(),
                            documentReference.get("idPk").toString());

            DataType typeData = DataType.valueOf(documentFieldReferenced.get("typeData").toString());
            sortField.setType(typeData);
        }
        switch (sortField.getType()) {
            case NUMBER_INTEGER:
            case NUMBER_DECIMAL:
                dataQuery.append(" order by CAST(").append(sortField.getName()).append(" as DECIMAL) ");
                break;
            case DATE:
                dataQuery.append(" order by CAST(").append(sortField.getName()).append(" as DATE) ");
                break;
            default:
                dataQuery.append(" order by ").append(sortField.getName());
                break;
        }
        dataQuery.append(sort[1].equals("1") ? "asc" : "desc");
    }

    /**
     * builds query according to filtering options
     * @param fieldValue
     * @param fieldIdMap
     * @param dataQuery
     */
    private static void buildFieldValueFilterQuery(String fieldValue, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        dataQuery.append(" where (");
        List<String> headers = fieldIdMap.values().stream().map(FieldSchemaVO::getName).collect(Collectors.toList());
        LOG.info("headers : {}", headers);
        dataQuery.append(headers.get(0)).append(" like '%").append(fieldValue).append("%'");
        LOG.info("headers.get(0) : {}", headers.get(0));
        headers.remove(headers.get(0));
        LOG.info("headers : {}", headers);
        headers.forEach(header -> dataQuery.append(" OR ").append(header).append(" like '%").append(fieldValue).append("%'"));
        dataQuery.append(")");
    }

    /**
     * finds tableSchemaVO
     * @param idTableSchema
     * @param datasetSchemaId
     * @return
     * @throws EEAException
     */
    @Override
    public TableSchemaVO getTableSchemaVO(String idTableSchema, String datasetSchemaId) throws EEAException {
        DataSetSchemaVO dataSetSchemaVO;
        try {
            dataSetSchemaVO = fileCommon.getDataSetSchemaVO(datasetSchemaId);
        } catch (EEAException e) {
            LOG.error("Error retrieving dataset schema for datasetSchemaId {}", datasetSchemaId);
            throw new EEAException(e);
        }
        Optional<TableSchemaVO> tableSchemaOptional = dataSetSchemaVO.getTableSchemas().stream().filter(t -> t.getIdTableSchema().equals(idTableSchema)).findFirst();
        TableSchemaVO tableSchemaVO = null;
        if (!tableSchemaOptional.isPresent()) {
            LOG.error("table with id {} not found in mongo results", idTableSchema);
            throw new EEAException("Error retrieving table with id " + idTableSchema);
        }
        tableSchemaVO = tableSchemaOptional.get();
        return tableSchemaVO;
    }

    private Pageable calculatePageable(Pageable pageable, Long totalRecords) {
        if (pageable == null && totalRecords > 0) {
            pageable = PageRequest.of(0, totalRecords.intValue());
        }
        return pageable;
    }
}
