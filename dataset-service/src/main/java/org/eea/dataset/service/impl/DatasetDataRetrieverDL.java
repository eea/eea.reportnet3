package org.eea.dataset.service.impl;

import cdjd.org.apache.commons.lang3.BooleanUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.DremioValidationMapper;
import org.eea.dataset.service.DataLakeDataRetriever;
import org.eea.dataset.service.DatasetTableService;
import org.eea.dataset.service.DremioAutoPromotionService;
import org.eea.dataset.util.DataLakeDataRetrieverUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.RedisLockController.RedisLockControllerZuul;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.validation.DremioValidationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum.REFERENCE;
import static org.eea.utils.LiteralConstants.*;

@Service
public class DatasetDataRetrieverDL implements DataLakeDataRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetDataRetrieverDL.class);
    private final S3Service s3Service;
    private final S3Helper s3Helper;
    private final JdbcTemplate dremioJdbcTemplate;
    private final DremioHelperService dremioHelperService;
    private final DatasetTableService datasetTableService;
    private final DremioAutoPromotionService dremioAutoPromotionService;

    public DatasetDataRetrieverDL(S3Service s3Service, S3Helper s3Helper, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, DremioHelperService dremioHelperService, DatasetTableService datasetTableService, RedisLockControllerZuul redisLockControllerZuul, JobControllerZuul jobControllerZuul, DremioAutoPromotionService dremioAutoPromotionService) {
        this.s3Service = s3Service;
        this.s3Helper = s3Helper;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.dremioHelperService = dremioHelperService;
        this.datasetTableService = datasetTableService;
        this.dremioAutoPromotionService = dremioAutoPromotionService;
    }

    @Override
    public TableVO getTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldSchemaId, String fieldValue, ErrorTypeEnum[] levelError,
                                  String[] qcCodes) throws EEAException {
        Long totalRecords = 0L;
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();
        S3PathResolver s3PathResolver;
        if(BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable()) && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))){
            s3PathResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema(), true);
            s3PathResolver.setIsIcebergTable(true);
        }
        else{
            s3PathResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema(), false);
            s3PathResolver.setIsIcebergTable(false);
        }

        boolean folderExist = s3Helper.checkFolderExist(s3PathResolver);

        if (folderExist) {
            // Try to auto promote if it’s safe and not already promoted.
            dremioAutoPromotionService.ensureSafeFolderPromotion(dataset, s3PathResolver);

            // Check for promotion again.
            if (dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
                StringBuilder dataQuery = new StringBuilder();
                StringBuilder recordsCountQuery = new StringBuilder();

                if (REFERENCE.equals(dataset.getDatasetTypeEnum()) && s3PathResolver.getIsIcebergTable() == false) {
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

                Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
                S3PathResolver validationS3PathResolver = new S3PathResolver(
                    dataset.getDataflowId(),
                    dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0,
                    dataset.getId(),
                    S3_VALIDATION
                );
                validationS3PathResolver.setIsIcebergTable(false);
                validationS3PathResolver.setPath(S3_TABLE_AS_FOLDER_QUERY_PATH);

                String validationTablePath = s3Service.getTableAsFolderQueryPath(validationS3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
                StringBuilder filteredQuery = DataLakeDataRetrieverUtils.buildFilteredQuery(dataset, fields, fieldSchemaId, fieldValue, fieldIdMap, levelError, qcCodes, validationTablePath);

                if (filteredQuery.toString().isEmpty() && levelError != null && levelError.length == 0) {
                    result.setTotalFilteredRecords(0L);
                    result.setTotalRecords(totalRecords);
                    result.setRecords(new ArrayList<>());
                } else {
                    recordsCountQuery.append(filteredQuery);
                    // Table path for metadata refresh and promotion.
                    String tablePathForRefresh;
                    if (REFERENCE.equals(dataset.getDatasetTypeEnum()) && !Boolean.TRUE.equals(s3PathResolver.getIsIcebergTable())) {
                        tablePathForRefresh = s3Service.getTableAsFolderQueryPath(s3PathResolver);
                    } else {
                        tablePathForRefresh = s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
                    }

                    loadTableWithRetry(dataset, tableSchemaVO, pageable, result, s3PathResolver, validationS3PathResolver,
                        dataQuery, recordsCountQuery, validationTablePath, filteredQuery, tablePathForRefresh);
                }
            } else {
                setEmptyResults(result);
            }
        } else {
            setEmptyResults(result);
        }
        return result;
    }

    /**
     * Helper that makes a second try to fetch the table data if it fails the first time.
     */
    private void loadTableWithRetry(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, TableVO result,
                                    S3PathResolver dataS3PathResolver, S3PathResolver validationS3PathResolver, StringBuilder dataQuery, StringBuilder recordsCountQuery,
                                    String validationTablePath, StringBuilder filteredQuery, String tablePathForRefresh) {
        try {
            // First attempt to get the table results.
            getTableResultsAndValidations(dataset, tableSchemaVO, pageable, result, dataS3PathResolver, validationS3PathResolver, dataQuery, recordsCountQuery, validationTablePath, filteredQuery);
        } catch (Exception e) {
            LOG.warn("First attempt to retrieve table data failed for datasetId {} table {}. Error: {}", dataset.getId(), tableSchemaVO.getNameTableSchema(), e.getMessage(), e);
            LOG.info("Trying to demote, refresh metadata, promote and retry retrieve table data once more.");
            dremioAutoPromotionService.demoteAndRefreshMetadataAndPromote(dataset, tablePathForRefresh, dataS3PathResolver);
            // Second and last try to get the table results.
            getTableResultsAndValidations(dataset, tableSchemaVO, pageable, result, dataS3PathResolver, validationS3PathResolver, dataQuery, recordsCountQuery, validationTablePath, filteredQuery);
        }
    }

    /**
     * Helper that makes a second try to fetch the table data if it fails the first time.
     */
    private void loadTableWithRetryPreparations(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, TableVO result,
                                    S3PathResolver dataS3PathResolver, S3PathResolver validationS3PathResolver, StringBuilder dataQuery, StringBuilder recordsCountQuery,
                                    String validationTablePath, StringBuilder filteredQuery, String tablePathForRefresh) {
        try {
            // First attempt to get the table results.
            getPreparationTableResultsAndValidations(dataset, tableSchemaVO, pageable, result, dataS3PathResolver, validationS3PathResolver, dataQuery, recordsCountQuery, validationTablePath, filteredQuery);
        } catch (Exception e) {
            LOG.warn("First attempt to retrieve table data failed for datasetId {} table {}. Error: {}", dataset.getId(), tableSchemaVO.getNameTableSchema(), e.getMessage(), e);
            LOG.info("Trying to demote, refresh metadata, promote and retry retrieve table data once more.");
            dremioAutoPromotionService.demoteAndRefreshMetadataAndPromote(dataset, tablePathForRefresh, dataS3PathResolver);
            // Second and last try to get the table results.
            getPreparationTableResultsAndValidations(dataset, tableSchemaVO, pageable, result, dataS3PathResolver, validationS3PathResolver, dataQuery, recordsCountQuery, validationTablePath, filteredQuery);
        }
    }

    /**
     * Gets table results and validations
     * @param dataset
     * @param tableSchemaVO
     * @param pageable
     * @param result
     * @param dataS3PathResolver
     * @param validationS3PathResolver
     * @param dataQuery
     * @param recordsCountQuery
     * @param validationTablePath
     * @param filteredQuery
     */
    private void getTableResultsAndValidations(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, TableVO result, S3PathResolver dataS3PathResolver,
                                               S3PathResolver validationS3PathResolver, StringBuilder dataQuery, StringBuilder recordsCountQuery, String validationTablePath, StringBuilder filteredQuery) {
        String recordsCountQueryString = recordsCountQuery.toString();
        int idx = recordsCountQueryString.indexOf("order by");
        if (idx!=-1) {
            recordsCountQueryString = recordsCountQueryString.substring(0, idx-1);
        }
        Long totalFilteredRecords = dremioJdbcTemplate.queryForObject(recordsCountQueryString, Long.class);
        result.setTotalFilteredRecords(totalFilteredRecords);

        pageable = DataLakeDataRetrieverUtils.calculatePageable(pageable, totalFilteredRecords);
        //pagination
        if (pageable !=null) {
            DataLakeDataRetrieverUtils.buildPaginationQuery(pageable, filteredQuery);
        }
        dataQuery.append(filteredQuery);
        List<RecordVO> recordVOS = DataLakeDataRetrieverUtils.getRecordVOS(dataset.getDatasetSchema(), tableSchemaVO, dataQuery);
        result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
        result.setRecords(recordVOS);

        //validations only exist in the parquet bucket
        dataS3PathResolver.setIsIcebergTable(false);
        if (s3Helper.checkFolderExist(validationS3PathResolver, S3_VALIDATION_TABLE_PATH)) {
            if (!dremioHelperService.checkFolderPromoted(validationS3PathResolver, S3_VALIDATION)) {
                dremioHelperService.promoteFolderOrFile(validationS3PathResolver, S3_VALIDATION);
            }
            if (!recordVOS.isEmpty()) {
                retrieveValidations(recordVOS, tableSchemaVO.getNameTableSchema(), validationTablePath);
            }
        }
    }

    private void getPreparationTableResultsAndValidations(
            DataSetMetabaseVO dataset,
            TableSchemaVO tableSchemaVO,
            Pageable pageable,
            TableVO result,
            S3PathResolver preparationDataResolver,
            S3PathResolver preparationValidationResolver,
            StringBuilder dataQuery,
            StringBuilder recordsCountQuery,
            String validationTablePath,
            StringBuilder filteredQuery) {

        // --- Count filtered records ---
        String countQuery = recordsCountQuery.toString();
        int orderByIndex = countQuery.indexOf("order by");
        if (orderByIndex != -1) {
            countQuery = countQuery.substring(0, orderByIndex);
        }

        Long totalFilteredRecords =
                dremioJdbcTemplate.queryForObject(countQuery, Long.class);

        result.setTotalFilteredRecords(totalFilteredRecords);

        // --- Pagination ---
        pageable = DataLakeDataRetrieverUtils.calculatePageable(
                pageable, totalFilteredRecords);

        if (pageable != null) {
            DataLakeDataRetrieverUtils.buildPaginationQuery(pageable, filteredQuery);
        }

        // --- Final data query ---
        dataQuery.append(filteredQuery);

        List<RecordVO> recordVOS =
                DataLakeDataRetrieverUtils.getRecordVOS(
                        dataset.getDatasetSchema(),
                        tableSchemaVO,
                        dataQuery);

        result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
        result.setRecords(recordVOS);

        // --- Validations (PREPARATION) ---
        preparationDataResolver.setIsIcebergTable(false);

        if (s3Helper.checkFolderExist(preparationValidationResolver, S3_PREPARATION_VALIDATION_TABLE_PATH)) {

            if (!dremioHelperService.checkFolderPromoted(
                    preparationValidationResolver, S3_VALIDATION)) {

                dremioHelperService.promoteFolderOrFile(
                        preparationValidationResolver, S3_VALIDATION);
            }

            if (!recordVOS.isEmpty()) {
                retrieveValidations(
                        recordVOS,
                        tableSchemaVO.getNameTableSchema(),
                        validationTablePath);
            }
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

    @Override
    public boolean isApplicable(String datasetType) {
        List<String> acceptedTypes = List.of(DatasetTypeEnum.DESIGN.getValue(), DatasetTypeEnum.REPORTING.getValue(), DatasetTypeEnum.REFERENCE.getValue(),
            DatasetTypeEnum.TEST.getValue());
        return acceptedTypes.stream().anyMatch(datasetType::equals);
    }

    @Override
    public TableVO getPreparationTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldSchemaId, String fieldValue, ErrorTypeEnum[] levelError,
                                  String[] qcCodes, String preparationCode) throws EEAException {
        Long totalRecords = 0L;
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();
        S3PathResolver s3PathResolverParentDataset;
        //parent dataset resolver
        if(BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable()) && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))){
            s3PathResolverParentDataset = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema(), true);
            s3PathResolverParentDataset.setIsIcebergTable(true);
        }
        else{
            s3PathResolverParentDataset = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema(), false);
            s3PathResolverParentDataset.setIsIcebergTable(false);
        }

        //preparations resolver
        S3PathResolver s3PathResolverPreparations = new S3PathResolver(dataset.getDataflowId(),
                dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0,
                dataset.getId(), tableSchemaVO.getNameTableSchema());
        s3PathResolverPreparations.setPath(S3_PREPARATION_TABLE_NAME_FOLDER_PATH);
        s3PathResolverPreparations.setPreparationCode(preparationCode);

        boolean preparationsIsIcebergTable = false; // to be substituted when preparations table editing available
        if(BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable()) && BooleanUtils.isTrue(preparationsIsIcebergTable)) {
            s3PathResolverPreparations.setIsIcebergTable(true);
        }
        else {
            s3PathResolverPreparations.setIsIcebergTable(false);
        }

        boolean preparationFolderExist = s3Helper.checkFolderExist(s3PathResolverPreparations);

        if (preparationFolderExist) {
            // Try to auto promote if it’s safe and not already promoted.
            dremioAutoPromotionService.ensureSafeFolderPromotion(dataset, s3PathResolverPreparations);

            // Check for promotion again.
            if (dremioHelperService.checkFolderPromoted(s3PathResolverPreparations, s3PathResolverPreparations.getTableName())) {
                StringBuilder dataQuery = new StringBuilder();
                StringBuilder recordsCountQuery = new StringBuilder();

                if (REFERENCE.equals(dataset.getDatasetTypeEnum()) && s3PathResolverParentDataset.getIsIcebergTable() == false) {
                    s3PathResolverParentDataset.setPath(S3_DATAFLOW_REFERENCE_QUERY_PATH);
                    totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.getRecordsCountQuery(s3PathResolverParentDataset), Long.class);
                    dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolverParentDataset) + " t ");
                    recordsCountQuery.append("select count(record_id) from " + s3Service.getTableAsFolderQueryPath(s3PathResolverParentDataset) + " t ");
                } else {
                    totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolverPreparations), Long.class);
                    //select * from "rn3-dataset"."rn3-dataset"."df-0000150"."dp-0000002"."ds-0001542"."current"."t1" t
                    dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolverPreparations, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH) + " t ");
                    recordsCountQuery.append("select count(record_id) from " + s3Service.getTableAsFolderQueryPath(s3PathResolverPreparations, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH) + " t ");
                }
                result.setTotalRecords(totalRecords);

                Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
                S3PathResolver validationS3PathResolverPreparations = new S3PathResolver(
                        dataset.getDataflowId(),
                        dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0,
                        dataset.getId(),
                        S3_VALIDATION
                );
                validationS3PathResolverPreparations.setPreparationCode(preparationCode);
                validationS3PathResolverPreparations.setIsIcebergTable(false);
                validationS3PathResolverPreparations.setPath(S3_PREPARATION_VALIDATION_TABLE_PATH);
                String validationTablePath = s3Service.getTableAsFolderQueryPath(validationS3PathResolverPreparations, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH);
                StringBuilder filteredQuery = DataLakeDataRetrieverUtils.buildFilteredQuery(dataset, fields, fieldSchemaId, fieldValue, fieldIdMap, levelError, qcCodes, validationTablePath);

                if (filteredQuery.toString().isEmpty() && levelError != null && levelError.length == 0) {
                    result.setTotalFilteredRecords(0L);
                    result.setTotalRecords(totalRecords);
                    result.setRecords(new ArrayList<>());
                } else {
                    recordsCountQuery.append(filteredQuery);
                    // Table path for metadata refresh and promotion.
                    String tablePathForRefresh;
                    if (REFERENCE.equals(dataset.getDatasetTypeEnum()) && !Boolean.TRUE.equals(s3PathResolverParentDataset.getIsIcebergTable())) {
                        tablePathForRefresh = s3Service.getTableAsFolderQueryPath(s3PathResolverParentDataset);
                    } else {
                        tablePathForRefresh = s3Service.getTableAsFolderQueryPath(s3PathResolverPreparations, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH);
                    }

                    loadTableWithRetryPreparations(dataset, tableSchemaVO, pageable, result, s3PathResolverPreparations, validationS3PathResolverPreparations,
                            dataQuery, recordsCountQuery, validationTablePath, filteredQuery, tablePathForRefresh);
                }
            } else {
                setEmptyResults(result);
            }
        } else {
            setEmptyResults(result);
        }
        return result;
    }

}
