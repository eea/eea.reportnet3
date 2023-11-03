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
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DataType;
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

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum.*;
import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DataLakeDataRetrieverServiceImpl implements DataLakeDataRetrieverService {

    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private JdbcTemplate dremioJdbcTemplate;
    private FileCommonUtils fileCommon;
    private S3Service s3Service;
    private SchemasRepository schemasRepository;
    private S3Client s3Client;
    private S3Helper s3Helper;
    private DremioHelperService dremioHelperService;

    @Autowired
    public DataLakeDataRetrieverServiceImpl(DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate,
                                            FileCommonUtils fileCommon, S3Service s3Service, SchemasRepository schemasRepository, S3Client s3Client, S3Helper s3Helper,
                                            DremioHelperService dremioHelperService) {
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.fileCommon = fileCommon;
        this.s3Service = s3Service;
        this.schemasRepository = schemasRepository;
        this.s3Client = s3Client;
        this.s3Helper = s3Helper;
        this.dremioHelperService = dremioHelperService;
    }

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DataLakeDataRetrieverServiceImpl.class);

    @Override
    @Transactional
    public TableVO getTableValuesDLById(final Long datasetId, final String idTableSchema,
                                        Pageable pageable, final String fields, ErrorTypeEnum[] levelError, String[] qcCodes,
                                        String fieldSchema, String fieldValue) throws EEAException {
        TableVO result = new TableVO();
        Long totalRecords = 0L;
        DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
        String datasetSchemaId = dataset.getDatasetSchema();
        TableSchemaVO tableSchemaVO = getTableSchemaVO(idTableSchema, datasetSchemaId);
        DremioRecordMapper recordMapper = new DremioRecordMapper();
        recordMapper.setRecordSchemaVO(tableSchemaVO.getRecordSchema()).setDatasetSchemaId(datasetSchemaId).setTableSchemaId(idTableSchema);
        if (COLLECTION.equals(dataset.getDatasetTypeEnum())) {
            totalRecords = getDCTotalRecords(datasetId, idTableSchema, pageable, fields, fieldValue, result, totalRecords, dataset, datasetSchemaId, tableSchemaVO, recordMapper);
        } else if (EUDATASET.equals(dataset.getDatasetTypeEnum())) {
            totalRecords = getEUTotalRecords(datasetId, idTableSchema, pageable, fields, fieldValue, result, totalRecords, dataset, datasetSchemaId, tableSchemaVO, recordMapper);
        } else {
            totalRecords = getDatasetTotalRecords(datasetId, idTableSchema, pageable, fields, fieldValue, result, totalRecords, dataset, datasetSchemaId, tableSchemaVO, recordMapper);
        }
        result.setTotalRecords(totalRecords);
        return result;
    }

    /**
     * Finds dataset total records
     * @param datasetId
     * @param idTableSchema
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param result
     * @param totalRecords
     * @param dataset
     * @param datasetSchemaId
     * @param tableSchemaVO
     * @param recordMapper
     * @return
     */
    private Long getDatasetTotalRecords(Long datasetId, String idTableSchema, Pageable pageable, String fields, String fieldValue, TableVO result, Long totalRecords, DataSetMetabaseVO dataset, String datasetSchemaId, TableSchemaVO tableSchemaVO, DremioRecordMapper recordMapper) {
        S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0, datasetId, tableSchemaVO.getNameTableSchema());
        if (REFERENCE.equals(dataset.getDatasetTypeEnum())) {
            s3PathResolver.setPath(S3_DATAFLOW_REFERENCE_FOLDER_PATH);
        } else {
            s3PathResolver.setPath(S3_TABLE_NAME_FOLDER_PATH);
        }
        boolean folderExist = s3Helper.checkFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH) : {}", folderExist);
        if (folderExist && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName() ,false)) {
            StringBuilder dataQuery = new StringBuilder();
            if (REFERENCE.equals(dataset.getDatasetTypeEnum())) {
                s3PathResolver.setPath(S3_DATAFLOW_REFERENCE_QUERY_PATH);
                totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.getRecordsCountQuery(s3PathResolver), Long.class);
                dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver));
            } else {
                totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class);
                dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            }

            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            pageable = calculatePageable(pageable, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            //filter value
            List<RecordVO> recordVOS = buildQueriesAndGetRecords(datasetId, idTableSchema, pageable, fields, fieldValue, result, datasetSchemaId, fieldIdMap, recordMapper, dataQuery);
            s3PathResolver.setTableName(S3_VALIDATION);
            if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH)) {
            if (!dremioHelperService.checkFolderPromoted(s3PathResolver, S3_VALIDATION, false)) {
                dremioHelperService.promoteFolderOrFile(s3PathResolver, S3_VALIDATION, false);
            }
                retrieveValidations(recordVOS, tableSchemaVO.getNameTableSchema(), s3PathResolver);
            }
        } else {
            setEmptyResults(result);
        }
        return totalRecords;
    }

    /**
     * Finds dataCollection total records
     * @param datasetId
     * @param idTableSchema
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param result
     * @param totalRecords
     * @param dataset
     * @param datasetSchemaId
     * @param tableSchemaVO
     * @param recordMapper
     * @return
     */
    private Long getDCTotalRecords(Long datasetId, String idTableSchema, Pageable pageable, String fields, String fieldValue, TableVO result, Long totalRecords, DataSetMetabaseVO dataset, String datasetSchemaId, TableSchemaVO tableSchemaVO, DremioRecordMapper recordMapper) {
        S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), datasetId, tableSchemaVO.getNameTableSchema(), S3_TABLE_NAME_ROOT_DC_FOLDER_PATH);
        boolean isFolderPromoted = dremioHelperService.checkFolderPromoted(s3PathResolver,s3PathResolver.getTableName(), false);
        s3PathResolver.setPath(S3_TABLE_NAME_DC_FOLDER_PATH);
        boolean folderExist = s3Helper.checkTableNameDCFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_DC_FOLDER_PATH) : {}", folderExist);
        if (isFolderPromoted && folderExist) {
            totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQueryDC(s3PathResolver), Long.class);
            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            pageable = calculatePageable(pageable, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            FieldSchemaVO fieldSchemaProviderCode = new FieldSchemaVO();
            fieldSchemaProviderCode.setName("data_provider_code");
            fieldIdMap.put("data_provider_code", fieldSchemaProviderCode);
            StringBuilder dataQuery = new StringBuilder();
            dataQuery.append("select * from " + s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH));
            buildQueriesAndGetRecords(datasetId, idTableSchema, pageable, fields, fieldValue, result, datasetSchemaId, fieldIdMap, recordMapper, dataQuery);
        } else {
            setEmptyResults(result);
        }
        return totalRecords;
    }

    /**
     * Finds EU dataset total records
     * @param datasetId
     * @param idTableSchema
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param result
     * @param totalRecords
     * @param dataset
     * @param datasetSchemaId
     * @param tableSchemaVO
     * @param recordMapper
     * @return
     */
    private Long getEUTotalRecords(Long datasetId, String idTableSchema, Pageable pageable, String fields, String fieldValue, TableVO result, Long totalRecords, DataSetMetabaseVO dataset, String datasetSchemaId, TableSchemaVO tableSchemaVO, DremioRecordMapper recordMapper) {
        S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), datasetId, tableSchemaVO.getNameTableSchema(), S3_EU_SNAPSHOT_ROOT_PATH);
        boolean isFolderPromoted = dremioHelperService.checkFolderPromoted(s3PathResolver,s3PathResolver.getTableName(), false);
        boolean folderExist = s3Helper.checkTableNameDCFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_DC_FOLDER_PATH) : {}", folderExist);
        if (isFolderPromoted && folderExist) {
            s3PathResolver.setPath(S3_TABLE_NAME_EU_QUERY_PATH);
            totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.getRecordsCountQuery(s3PathResolver), Long.class);
            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            pageable = calculatePageable(pageable, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            FieldSchemaVO fieldSchemaProviderCode = new FieldSchemaVO();
            fieldSchemaProviderCode.setName("data_provider_code");
            fieldIdMap.put("data_provider_code", fieldSchemaProviderCode);
            StringBuilder dataQuery = new StringBuilder();
            dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver));
            buildQueriesAndGetRecords(datasetId, idTableSchema, pageable, fields, fieldValue, result, datasetSchemaId, fieldIdMap, recordMapper, dataQuery);
        } else {
            setEmptyResults(result);
        }
        return totalRecords;
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
     * @param datasetId
     * @param idTableSchema
     * @param pageable
     * @param fields
     * @param fieldValue
     * @param result
     * @param datasetSchemaId
     * @param fieldIdMap
     * @param recordMapper
     * @param dataQuery
     * @return
     */
    private List<RecordVO> buildQueriesAndGetRecords(Long datasetId, String idTableSchema, Pageable pageable, String fields, String fieldValue, TableVO result, String datasetSchemaId,
                                                     Map<String, FieldSchemaVO> fieldIdMap, DremioRecordMapper recordMapper, StringBuilder dataQuery) {
        //filter value
        if (!fieldValue.equals("")) {
            buildFilterQuery(fieldValue, fieldIdMap, dataQuery);
        }
        //sorting
        if (fields !=null) {
            buildSortQuery(fields, datasetSchemaId, fieldIdMap, dataQuery);
        }
        //pagination
        if (pageable !=null) {
            buildPaginationQuery(pageable, dataQuery);
        }
        LOG.info("For datasetId {} dataQuery.toString() : {}", datasetId, dataQuery);
        List<RecordVO> recordVOS = dremioJdbcTemplate.query(dataQuery.toString(), recordMapper);
        result.setIdTableSchema(idTableSchema);
        result.setRecords(recordVOS);
        result.setTotalFilteredRecords(Long.valueOf(recordVOS.size()));
        return recordVOS;
    }

    /**
     * retrieves validation results from dremio validation folder and maps them to fieldVO objects
     * @param recordVOS
     * @param tableName
     * @param s3PathResolver
     */
    private void retrieveValidations(List<RecordVO> recordVOS, String tableName, S3PathResolver s3PathResolver) {
        StringBuilder validationQuery = new StringBuilder();
        validationQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
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
           List<RecordVO> records =  recordVOS.stream().filter(recordVO -> recordVO.getId().equals(dv.getRecordId())).collect(Collectors.toList());
           records.forEach(vr -> vr.getFields().parallelStream().forEach(fieldVO -> {
               if (fieldVO.getName().equals(dv.getFieldName())) {
                   FieldValidationVO fieldValidationVO = new FieldValidationVO();
                   List<FieldValidationVO> fieldValidations = new ArrayList<>();
                   ValidationVO validationVO = new ValidationVO();
                   validationVO.setId(dv.getPk());
                   validationVO.setLevelError(dv.getValidationLevel());
                   validationVO.setMessage(dv.getMessage());
                   validationVO.setTypeEntity(dv.getValidationArea());
                   fieldValidationVO.setValidation(validationVO);
                   if (fieldVO.getFieldValidations()!=null) {
                       fieldValidations = fieldVO.getFieldValidations();
                   }
                   fieldValidations.add(fieldValidationVO);
                   fieldVO.setFieldValidations(fieldValidations);
               }
           }));
        }
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
    private static void buildFilterQuery(String fieldValue, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        dataQuery.append(" where ");
        List<String> headers = fieldIdMap.values().stream().map(FieldSchemaVO::getName).collect(Collectors.toList());
        LOG.info("headers : {}", headers);
        dataQuery.append(headers.get(0)).append(" like '%").append(fieldValue).append("%'");
        LOG.info("headers.get(0) : {}", headers.get(0));
        headers.remove(headers.get(0));
        LOG.info("headers : {}", headers);
        headers.forEach(header -> dataQuery.append(" OR ").append(header).append(" like '%").append(fieldValue).append("%'"));
    }

    /**
     * finds tableSchemaVO
     * @param idTableSchema
     * @param datasetSchemaId
     * @return
     * @throws EEAException
     */
    private TableSchemaVO getTableSchemaVO(String idTableSchema, String datasetSchemaId) throws EEAException {
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
