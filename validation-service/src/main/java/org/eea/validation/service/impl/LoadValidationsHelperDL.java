package org.eea.validation.service.impl;

import org.apache.commons.lang.StringUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.RedisLockController.RedisLockControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.eea.validation.service.DataLakeValidationService;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Component
public class LoadValidationsHelperDL {

    private final DataLakeValidationService dataLakeValidationService;
    private final DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private final S3Helper s3Helper;
    private final JdbcTemplate dremioJdbcTemplate;
    private final DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private final DremioHelperService dremioHelperService;
    private final JobControllerZuul jobControllerZuul;
    private final RedisLockService redisLockService;
    private final ValidationService validationService;

    @Autowired
    public LoadValidationsHelperDL(DataLakeValidationService dataLakeValidationService,
                                   DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul,
                                   S3Helper s3Helper,
                                   @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate,
                                   DatasetSchemaControllerZuul datasetSchemaControllerZuul,
                                   DremioHelperService dremioHelperService,
                                   JobControllerZuul jobControllerZuul1,
                                   RedisLockService redisLockService,
                                   @Qualifier("proxyValidationService") ValidationService validationService) {
        this.dataLakeValidationService = dataLakeValidationService;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.s3Helper = s3Helper;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dremioHelperService = dremioHelperService;
        this.jobControllerZuul = jobControllerZuul1;
        this.redisLockService = redisLockService;
        this.validationService = validationService;
    }

    private static final Logger LOG = LoggerFactory.getLogger(LoadValidationsHelperDL.class);

    public FailedValidationsDatasetVO getListGroupValidationsDL(Long datasetId, Pageable pageable, List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
                                                                String tableFilter, String fieldValueFilter, String shortCode, String headerField, Boolean asc, String preparationCode) throws EEAException {
        DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
        FailedValidationsDatasetVO validation = new FailedValidationsDatasetVO();
        validation.setErrors(new ArrayList<>());
        validation.setIdDatasetSchema(dataset.getDatasetSchema());
        validation.setIdDataset(datasetId);

        // Resolver pointing to the validation dataset.
        S3PathResolver s3PathResolver = new S3PathResolver(
            dataset.getDataflowId(),
            dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0,
            dataset.getId(),
            S3_VALIDATION);
        s3PathResolver.setPreparationCode(preparationCode);

        boolean validationFolderExists;
        if (StringUtils.isNotBlank(preparationCode)) {
            validationFolderExists = s3Helper.checkFolderExist(s3PathResolver, S3_PREPARATION_VALIDATION_TABLE_PATH);
        }
        else {
            validationFolderExists = s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH);
        }
        // First folder check.
        if (validationFolderExists && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
            // Load schema once
            DataSetSchemaVO schema = datasetSchemaControllerZuul.findDataSchemaByDatasetId(datasetId);

            // Ensure that sibling data tables are promoted.
            promoteSiblingDataTables(dataset, schema);

            // Main grouped validations.
            List<GroupValidationVO> errors = dataLakeValidationService.findGroupRecordsByFilter(s3PathResolver, levelErrorsFilter, typeEntitiesFilter, tableFilter,
                fieldValueFilter, shortCode, pageable, headerField, asc, true);
            validationService.setRuleMessageDL(schema.getIdDataSetSchema(), errors);
            validation.setErrors(errors);
            validation.setTotalErrors(dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class));
            validation.setTotalFilteredRecords(Long.valueOf(dataLakeValidationService.findGroupRecordsByFilter(s3PathResolver, levelErrorsFilter,
                typeEntitiesFilter, tableFilter, fieldValueFilter, shortCode, pageable, headerField, asc, false).size()));
            List<String> tableNames = schema.getTableSchemas().stream().map(TableSchemaVO::getNameTableSchema).collect(Collectors.toList());
            AtomicReference<Long> totalRecords = new AtomicReference<>(0L);
            tableNames.forEach(name -> {
                s3PathResolver.setTableName(name);
                if (StringUtils.isNotBlank(preparationCode)) {
                    if (s3Helper.checkFolderExist(s3PathResolver, S3_PREPARATION_TABLE_NAME_FOLDER_PATH)) {
                        Long tableRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class);
                        totalRecords.set(Long.sum(totalRecords.get(),tableRecords));
                    }
                }
                else {
                    if (s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                        Long tableRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQuery(s3PathResolver), Long.class);
                        totalRecords.set(Long.sum(totalRecords.get(),tableRecords));
                    }
                }
            });
            validation.setTotalRecords(totalRecords.get());
        }

        LOG.info("Total validations founded in datasetId {}: {}. Now in page {}, {} validation errors by page",
            datasetId, validation.getErrors().size(), pageable.getPageNumber(), pageable.getPageSize());

        return validation;
    }

    /**
     * Ensures that all data tables that live in the same directory as the validation folder
     * are promoted in Dremio, so that validations referencing them can work.
     */
    private void promoteSiblingDataTables(DataSetMetabaseVO dataset, DataSetSchemaVO schema) {
        // Prerequisites check.
        if (!canAutoPromoteSiblingTables(dataset)) {
            return;
        }

        Long datasetId = dataset.getId();
        Long dataflowId = dataset.getDataflowId();
        Long providerId = dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0L;
        schema.getTableSchemas().forEach(tableSchema -> {
            String tableName = tableSchema.getNameTableSchema();
            try {
                S3PathResolver tableResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableName);
                tableResolver.setIsIcebergTable(false);
                tableResolver.setPath(S3_TABLE_NAME_FOLDER_PATH);
                boolean folderExists = s3Helper.checkFolderExist(tableResolver, S3_TABLE_NAME_FOLDER_PATH);
                // Second folder check.
                if (!folderExists) {
                    LOG.warn("Table folder does not exist for datasetId {} table {} – skipping promotion", datasetId, tableName);
                    return;
                }

                if (dremioHelperService.checkFolderPromoted(tableResolver, tableName)) {
                    return;
                }

                LOG.info("Auto promoting data table {} for datasetId {} (sibling of validation folder)", tableName, datasetId);
                dremioHelperService.promoteFolderOrFile(tableResolver, tableName);
            } catch (Exception e) {
                LOG.error("Error auto-promoting sibling data table {} for datasetId {}: {}", tableName, datasetId, e.getMessage(), e);
            }
        });
    }

    private boolean canAutoPromoteSiblingTables(DataSetMetabaseVO dataset) {
        if (hasParquetConversionLock(dataset)) {
            LOG.info("Skipping sibling table auto promotion for datasetId {} due to PARQUET_CONVERSION lock", dataset.getId());
            return false;
        }
        if (hasBlockingJobs(dataset)) {
            LOG.info("Skipping sibling table auto promotion for datasetId {} due to active jobs", dataset.getId());
            return false;
        }
        return true;
    }

    private boolean hasParquetConversionLock(DataSetMetabaseVO dataset) {
        Long datasetId = dataset.getId();
        try {
            String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
            Map<String, String> activeLocks = redisLockService.listActiveLocks(lockKey);
            boolean locked = activeLocks != null && !activeLocks.isEmpty();
            if (locked) {
                LOG.info("Found redis locks for datasetId {}: {}", datasetId, activeLocks);
            }
            return locked;
        } catch (Exception e) {
            LOG.error("Error checking redis locks for datasetId {}", datasetId, e);
            return false;
        }
    }

    private boolean hasBlockingJobs(DataSetMetabaseVO dataset) {
        try {
            Long datasetId = dataset.getId();
            Long dataflowId = dataset.getDataflowId();
            Long providerId = dataset.getDataProviderId();

            List<JobVO> activeJobs = jobControllerZuul.findActiveJobsRelatedToADatasetId(datasetId, dataflowId, providerId);

            if (activeJobs == null || activeJobs.isEmpty()) {
                return false;
            }

            boolean blocked = activeJobs.stream().anyMatch(job ->
                job.getJobType() == JobTypeEnum.IMPORT ||
                    job.getJobType() == JobTypeEnum.DELETE ||
                    job.getJobType() == JobTypeEnum.VALIDATION ||
                    job.getJobType() == JobTypeEnum.ETL_IMPORT
            );

            if (blocked) {
                LOG.info("Blocking jobs for datasetId {}", datasetId);
            }
            return blocked;
        } catch (Exception e) {
            LOG.error("Error checking active jobs for datasetId {}: {}", dataset.getId(), e.getMessage(), e);
            return false;
        }
    }

}
