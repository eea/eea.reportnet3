package org.eea.dataset.service.impl;

import cdjd.org.apache.commons.lang3.BooleanUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.DremioValidationMapper;
import org.eea.dataset.service.DataLakeDataRetriever;
import org.eea.dataset.service.DatasetTableService;
import org.eea.dataset.util.DataLakeDataRetrieverUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.validation.DremioValidationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private S3Service s3Service;
    private S3Helper s3Helper;
    private JdbcTemplate dremioJdbcTemplate;
    private DremioHelperService dremioHelperService;

    @Autowired
    private DatasetTableService datasetTableService;

    @Autowired
    public DatasetDataRetrieverDL(S3Service s3Service, S3Helper s3Helper, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, DremioHelperService dremioHelperService) {
        this.s3Service = s3Service;
        this.s3Helper = s3Helper;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.dremioHelperService = dremioHelperService;
    }

    @Override
    public TableVO getTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldValue, ErrorTypeEnum[] levelError,
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
        if (folderExist && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
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

            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            s3PathResolver.setTableName(S3_VALIDATION);
            //validations only exist in the parquet bucket
            s3PathResolver.setIsIcebergTable(false);
            String validationTablePath = s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
            s3PathResolver.setIsIcebergTable(true);
            StringBuilder filteredQuery = DataLakeDataRetrieverUtils.buildFilteredQuery(dataset, fields, fieldValue, fieldIdMap, levelError, qcCodes, validationTablePath);
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
        s3PathResolver.setIsIcebergTable(false);
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
}
