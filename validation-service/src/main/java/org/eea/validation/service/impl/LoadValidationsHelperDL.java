package org.eea.validation.service.impl;

import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.multitenancy.DatasetId;
import org.eea.validation.service.DataLakeValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Component
public class LoadValidationsHelperDL {

    private DataLakeValidationService dataLakeValidationService;
    private S3Service s3Service;
    private S3Client s3Client;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private S3Helper s3Helper;
    private JdbcTemplate dremioJdbcTemplate;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DremioHelperService dremioHelperService;

    @Autowired
    public LoadValidationsHelperDL(DataLakeValidationService dataLakeValidationService, S3Service s3Service, S3Client s3Client, DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul,
                                   S3Helper s3Helper, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, DatasetSchemaControllerZuul datasetSchemaControllerZuul, DremioHelperService dremioHelperService) {
        this.dataLakeValidationService = dataLakeValidationService;
        this.s3Service = s3Service;
        this.s3Client = s3Client;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.s3Helper = s3Helper;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dremioHelperService = dremioHelperService;
    }

    private static final Logger LOG = LoggerFactory.getLogger(LoadValidationsHelperDL.class);

    public FailedValidationsDatasetVO getListGroupValidationsDL(@DatasetId Long datasetId,
                                                              Pageable pageable, List<ErrorTypeEnum> levelErrorsFilter,
                                                              List<EntityTypeEnum> typeEntitiesFilter, String tableFilter, String fieldValueFilter,
                                                              String headerField, Boolean asc) throws EEAException {
        DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
        FailedValidationsDatasetVO validation = new FailedValidationsDatasetVO();
        validation.setErrors(new ArrayList<>());
        validation.setIdDatasetSchema(dataset.getDatasetSchema());
        validation.setIdDataset(datasetId);

        S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0, dataset.getId(), S3_VALIDATION);
        if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH) && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName(), false)) {
            List<GroupValidationVO> errors = dataLakeValidationService.findGroupRecordsByFilter(s3PathResolver, levelErrorsFilter, typeEntitiesFilter, tableFilter,
                    fieldValueFilter, pageable, headerField, asc, true);
            validation.setErrors(errors);
            validation.setTotalErrors(dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class));
            DataSetSchemaVO schema = datasetSchemaControllerZuul.findDataSchemaByDatasetId(datasetId);
            List<String> tableNames = schema.getTableSchemas().stream().map(tableSchemaVO -> tableSchemaVO.getNameTableSchema()).collect(Collectors.toList());
            AtomicReference<Long> totalRecords = new AtomicReference<>(0L);
            tableNames.forEach(name -> {
                s3PathResolver.setTableName(name);
                if (s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                    Long tableRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class);
                    totalRecords.set(Long.sum(totalRecords.get(),tableRecords));
                }
            });
            validation.setTotalRecords(totalRecords.get());
            if (s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                validation.setTotalFilteredRecords(
                        Long.valueOf(dataLakeValidationService.findGroupRecordsByFilter(s3PathResolver, levelErrorsFilter,
                                        typeEntitiesFilter, tableFilter, fieldValueFilter, pageable, headerField, asc, false)
                                .size()));
            }
        }
        LOG.info(
                "Total validations founded in datasetId {}: {}. Now in page {}, {} validation errors by page",
                datasetId, validation.getErrors().size(), pageable.getPageNumber(), pageable.getPageSize());
        return validation;
    }
}
