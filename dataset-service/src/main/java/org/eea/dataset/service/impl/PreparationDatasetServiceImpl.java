package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.PreparationDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.PreparationDataset;
import org.eea.dataset.persistence.metabase.repository.PreparationDatasetRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.PreparationDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetResponseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.eea.utils.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.eea.utils.LiteralConstants.*;

/**
 * The Class PreparationDatasetServiceImpl.
 */
@Service
public class PreparationDatasetServiceImpl implements PreparationDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(PreparationDatasetServiceImpl.class);
    /** The service instance id. */
    @Value("${redis.lock.preparation.datasets.creation.expireTimeInMillis}")
    private long expireTimeInMillis;

    private final PreparationDatasetRepository preparationDatasetRepository;
    private final DremioHelperService dremioHelperService;
    private final DatasetMetabaseService datasetMetabaseService;
    private final DatasetSchemaService datasetSchemaService;
    private final S3Service s3ServicePrivate;
    private final S3Helper s3HelperPrivate;
    private final RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;
    private final KafkaSenderUtils kafkaSenderUtils;
    private final PreparationDatasetMapper preparationDatasetMapper;
    private final RedisLockService redisLockService;

    @Autowired
    public PreparationDatasetServiceImpl(PreparationDatasetRepository preparationDatasetRepository,
                                         DremioHelperService dremioHelperService,
                                         DatasetMetabaseService datasetMetabaseService,
                                         DatasetSchemaService datasetSchemaService,
                                         S3Helper s3HelperPrivate,
                                         RepresentativeController.RepresentativeControllerZuul representativeControllerZuul,
                                         KafkaSenderUtils kafkaSenderUtils,
                                         PreparationDatasetMapper preparationDatasetMapper,
                                         RedisLockService redisLockService) {
        this.preparationDatasetRepository = preparationDatasetRepository;
        this.dremioHelperService = dremioHelperService;
        this.datasetMetabaseService = datasetMetabaseService;
        this.datasetSchemaService = datasetSchemaService;
        this.s3HelperPrivate = s3HelperPrivate;
        this.s3ServicePrivate = s3HelperPrivate.getS3Service();
        this.representativeControllerZuul = representativeControllerZuul;
        this.kafkaSenderUtils = kafkaSenderUtils;
        this.preparationDatasetMapper = preparationDatasetMapper;
        this.redisLockService = redisLockService;
    }

    @Override
    @Transactional
    public PreparationDatasetResponseVO findPreparationDatasets(
            Long dataflowId,
            Long providerId,
            String code) {

        List<PreparationDataset> entities;

        if (StringUtils.isNotBlank(code)) {
            entities = preparationDatasetRepository
                    .findByDataflowIdAndProviderIdAndCode(dataflowId, providerId, code);
        } else if (providerId != null) {
            entities = preparationDatasetRepository
                    .findByDataflowIdAndProviderId(dataflowId, providerId);
        } else {
            entities = preparationDatasetRepository
                    .findByDataflowId(dataflowId);
        }

        List<PreparationDatasetVO> preparationDatasetVOS = entities.stream().map(preparationDatasetMapper::entityToClass).collect(Collectors.toList());
        String lockKey = LockEnum.PREPERATION_DATASET_CREATION.getValue() + "_" + dataflowId + "_" + providerId;
        Map<String, String> activeLocks = redisLockService.listActiveLocks(lockKey);

        return new PreparationDatasetResponseVO(preparationDatasetVOS, activeLocks);
    }

    @Override
    public List<PreparationDatasetVO> findByDataflowIdAndProviderIdAndIsCreated(Long dataflowId, Long providerId, Boolean isCreated) {
        if (isCreated == null) { isCreated = false; }

        return preparationDatasetMapper.entityListToClass(preparationDatasetRepository.findByDataflowIdAndProviderIdAndIsCreated(dataflowId, providerId, isCreated));
    }


    @Override
    @Transactional
    public void createPreparationDataset(Long dataflowId, Long parentDatasetId, PreparationDatasetVO vo) throws EEAException {

        if (preparationDatasetRepository.existsByDataflowIdAndProviderIdAndCode(dataflowId, vo.getProviderId(), vo.getCode())) {
            throw new EEAException("Preparation dataset with this code already exists");
        }

        PreparationDataset preparationDataset = new PreparationDataset();
        preparationDataset.setDataflowId(dataflowId);
        preparationDataset.setProviderId(vo.getProviderId());
        preparationDataset.setParentDatasetId(parentDatasetId);
        preparationDataset.setCode(vo.getCode());
        preparationDataset.setDatasetName(vo.getDatasetName());
        preparationDataset.setIsCreated(Boolean.TRUE.equals(vo.getIsCreated()));

        preparationDatasetRepository.save(preparationDataset);
    }

    @Override
    @Transactional
    public void deletePreparationDatasetById(Long preparationId) throws Exception {
        PreparationDataset preparationDataset = preparationDatasetRepository
                .findById(preparationId)
                .orElseThrow(() -> new EEAException("Preparation dataset not found"));

        final Long dataflowId = preparationDataset.getDataflowId();
        final Long providerId = preparationDataset.getProviderId();
        final String preparationCode = preparationDataset.getCode();

        LOG.info("Deleting preparation dataset with id {}, dataflowId {}, providerId {}, and code {}", preparationId, dataflowId, providerId, preparationCode);

        // fetch all related parent datasets
        List<DataSetMetabaseVO> datasetMetabaseVOS = datasetMetabaseService.getDatasetsByDataflowIdAndProviderId(dataflowId, providerId);

        if (datasetMetabaseVOS.isEmpty()) {
            throw new EEAException("Parent dataset metadata not found for preparation dataset");
        }

        for (DataSetMetabaseVO datasetMetabaseVO : datasetMetabaseVOS) {
            final Long parentDatasetId = datasetMetabaseVO.getId();
            // fetch all tables
            List<TableSchemaIdNameVO> parentTables = datasetSchemaService.getTableSchemasIds(parentDatasetId);

            // iterate all tables and delete them one by one
            LOG.info("Preparation dataset with parent datasetId {} has {} tables, proceeding to demotion and deletion if exists. Tables of parent dataset are: {}", parentDatasetId, parentTables.size(), parentTables.stream().map(TableSchemaIdNameVO::getNameTableSchema).collect(Collectors.toList()));
            for (TableSchemaIdNameVO parentTable : parentTables) {
                if (parentTable.getIdTableSchema() == null) {
                    LOG.warn("Skipping deletion: parent table schema not found");
                    continue;
                }

                final String parentTableName = parentTable.getNameTableSchema();
                S3PathResolver preparationTableS3Path = new S3PathResolver(dataflowId, providerId, parentDatasetId, parentTableName, parentTableName, preparationCode, S3_PREPARATION_TABLE_NAME_FOLDER_PATH);
                String preparationTableDremioQueryPathString = s3ServicePrivate.getTableAsFolderQueryPath(preparationTableS3Path, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH);
                // if there are no tables to delete skip them (empty tables have no dremio objects)

                if (!s3HelperPrivate.checkFolderExist(preparationTableS3Path, S3_PREPARATION_TABLE_NAME_FOLDER_PATH)) {
                    LOG.info("Preparation table {} does not exist for datasetId {}, nothing to delete, skipping", parentTableName, parentDatasetId);
                    continue;
                }
                LOG.info("Preparation table {} exist for datasetId {}. Starting demotion and deletion", parentTableName, parentDatasetId);

                dropDremioTable(preparationTableDremioQueryPathString);

//                try {
//                    dremioHelperService.demoteFolderOrFile(preparationTableS3Path, parentTableName);
//                } catch (Exception ex) {
//                    // if for any reason it cannot be demoted we cannot continue
//                    LOG.error("Failed to demote Dremio preparation table before deletion for datasetId {} and table {}", parentDatasetId, parentTableName, ex);
//                    throw new EEAException("Failed to demote preparation table in Dremio", ex);
//                }
//
//                try {
//                    s3HelperPrivate.deleteFolder(preparationTableS3Path, S3_PREPARATION_TABLE_NAME_FOLDER_PATH);
//                } catch (Exception ex) {
//                    LOG.error("Failed to delete S3 folder for preparation table for datasetId {} and table {}", parentDatasetId, parentTableName, ex);
//                    throw new EEAException("Failed to delete preparation table data from S3", ex);
//                }
                LOG.info("Preparation table {} demoted and deleted successfully for datasetId {}", parentTableName, parentDatasetId);
            }
        }

        // delete metabase db record as a final step
        preparationDatasetRepository.deleteById(preparationId);
        LOG.info("Successfully deleted preparation dataset with id {}, dataflowId {}, providerId {}, and code {}", preparationId, dataflowId, providerId, preparationCode);
    }

    @Override
    @Transactional
    @Async
    public void createAllEligiblePreparationSets(Long dataflowId, Long providerId) throws EEAException {
        LOG.info("Creating preparation sets for dataflow {} and providerId {}", dataflowId, providerId);
        NotificationVO notificationVO = NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .dataflowId(dataflowId)
                .providerId(providerId)
                .build();

        String lockKey = LockEnum.PREPERATION_DATASET_CREATION.getValue() + "_" + dataflowId + "_" + providerId;
        String lockValue = LockEnum.PREPERATION_DATASET_CREATION.getValue()  + "_" + dataflowId + "_" + providerId + "_" + UUID.randomUUID();
        if(!redisLockService.checkAndAcquireLock(lockKey, lockValue, expireTimeInMillis)) {
            Map<String, String> activeLocks = redisLockService.listActiveLocks(lockKey);
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.ANOTHER_PREPARATION_DATASET_CREATION_IS_RUNNING_FAILED_EVENT, null, notificationVO);
            throw new EEAException("Lock acquisition failed. Relative active locks: "+ activeLocks.toString());
        }

        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_STARTED_EVENT, null, notificationVO);

        // fetch all preparations datasets that haven't yet been created, `isCreated=false`
        List<PreparationDatasetVO> preparationDatasetVOS = findByDataflowIdAndProviderIdAndIsCreated(dataflowId, providerId, false);
        if (preparationDatasetVOS.isEmpty()) {
            redisLockService.releaseLock(lockKey, lockValue);
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_HAS_EMPTY_QUEUE_EVENT,null, notificationVO);
            LOG.info("No preparation dataset found without isCreated=FALSE for dataflow {} and providerId {}", dataflowId, providerId);
            return;
        }
        LOG.info("Creating {} preparation datasets for dataflow {} and providerId {}", preparationDatasetVOS.size(), dataflowId, providerId);

        List<DataSetMetabaseVO> datasetMetabaseVOS = datasetMetabaseService.getDatasetsByDataflowIdAndProviderId(dataflowId, providerId);

        // collect all exceptions in order to throw error notification only once.
        // this is because we are looping through many items but only want 1 notification at the front-end
        List<Exception> exceptions = new ArrayList<>();

        for (PreparationDatasetVO preparationDataset : preparationDatasetVOS) {
            try {
                createPreparationDataset(preparationDataset, datasetMetabaseVOS, notificationVO);
                markPreparationDatasetAsCreated(preparationDataset);
            } catch (Exception ex) {
                exceptions.add(ex);
                LOG.error("Preparation dataset creation failed for code {}: {}", preparationDataset.getCode(), ex.getMessage(), ex);
            }
        }

        // release lock and send notifications
        redisLockService.releaseLock(lockKey, lockValue);
        if (exceptions.isEmpty()) {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_COMPLETED_EVENT, null, notificationVO);
            LOG.info("Successfully created all preparation sets for dataflow {} and providerId {}", dataflowId, providerId);
        } else {
            try { kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_FAILED_EVENT, null, notificationVO);
            } catch (EEAException kafkaEx) { throw new RuntimeException("Failed to send failure notification", kafkaEx); }
            throw new RuntimeException("One or more preparation datasets failed. Total failures: " + exceptions.size() + ". Exception thrown: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
    }

    private void createPreparationDataset(PreparationDatasetVO preparationDataset, List<DataSetMetabaseVO> datasets, NotificationVO notificationVO) throws Exception {
        for (DataSetMetabaseVO dataset : datasets) {
            try {
                copyParentDatasetDataToPreparationDataset(dataset, preparationDataset.getCode());
                LOG.info("Copied tables for datasetId {} and preparationCode {}", dataset.getId(), preparationDataset.getCode());
            } catch (Exception ex) {
                notificationVO.setDatasetId(dataset.getId());
                notificationVO.setDatasetName(dataset.getDataSetName());
                notificationVO.setPreparationCode(preparationDataset.getCode());
                LOG.error("Creation failed for datasetId {} and code {}", dataset.getId(), preparationDataset.getCode(), ex);
                throw ex;
            }
        }
        LOG.info("Preparation dataset with code {} completed", preparationDataset.getCode());
    }

    private void markPreparationDatasetAsCreated(PreparationDatasetVO preparationDataset) {
        preparationDataset.setIsCreated(Boolean.TRUE);
        preparationDatasetRepository.save(preparationDatasetMapper.classToEntity(preparationDataset));
    }

    @Override
    public void copyParentDatasetDataToPreparationDataset(DataSetMetabaseVO parentDataSetMetabaseVO, String preparationCode) throws Exception {
        long providerId = parentDataSetMetabaseVO.getDataProviderId() == null ? 0 : parentDataSetMetabaseVO.getDataProviderId();
        if (providerId == 0) {
            LOG.info("Cannot create preparation datasets for DESIGN datasetId: {}",parentDataSetMetabaseVO.getId());
            return;
        }
        String parentDatasetSchemaId = parentDataSetMetabaseVO.getDatasetSchema();
        Long parentDatasetId = parentDataSetMetabaseVO.getId();

        List<TableSchemaIdNameVO> parentTablesList = datasetSchemaService.getTableSchemasIds(parentDatasetId);

        for (TableSchemaIdNameVO parentTable : parentTablesList) {
            TableSchemaVO parentTableSchema = datasetSchemaService.getTableSchemaVO(parentTable.getIdTableSchema(), parentDatasetSchemaId);
            if (parentTableSchema == null) {
                LOG.error("Parent table schema is null for IdTableSchema {} and parentDatasetSchemaId {}", parentTable.getIdTableSchema(), parentDatasetSchemaId);
                continue;
            }

            String parentTableName = parentTableSchema.getNameTableSchema();

            S3PathResolver sourceParentTableDremioQueryPath = new S3PathResolver(parentDataSetMetabaseVO.getDataflowId(), providerId, parentDatasetId, parentTableName, parentTableName, S3_TABLE_AS_FOLDER_QUERY_PATH);
            String sourceParentTableDremioQueryPathString = s3ServicePrivate.getTableAsFolderQueryPath(sourceParentTableDremioQueryPath, S3_TABLE_AS_FOLDER_QUERY_PATH);

            S3PathResolver sourceParentTableS3FolderPath = new S3PathResolver(parentDataSetMetabaseVO.getDataflowId(), providerId, parentDatasetId, parentTableName, parentTableName, S3_TABLE_NAME_FOLDER_PATH);

            S3PathResolver targetPreparationTableDremioQueryPath = new S3PathResolver(parentDataSetMetabaseVO.getDataflowId(), providerId, parentDatasetId, parentTableName, parentTableName, preparationCode, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH);
            String targetPreparationTableDremioQueryPathString = s3ServicePrivate.getTableAsFolderQueryPath(targetPreparationTableDremioQueryPath, S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH);

            S3PathResolver targetPreparationTableS3FolderPath = new S3PathResolver(parentDataSetMetabaseVO.getDataflowId(), providerId, parentDatasetId, parentTableName, parentTableName, preparationCode, S3_PREPARATION_TABLE_NAME_FOLDER_PATH);

            // if the parent dataset has no data then there is no file to copy, continue
            if (!s3HelperPrivate.checkFolderExist(sourceParentTableDremioQueryPath, S3_TABLE_NAME_FOLDER_PATH)) {
                LOG.info("No tables to copy for datasetId {} and parent table {}, skipping this dataset. Extra info: idTableSchema {}", parentDatasetId, parentTable.getNameTableSchema(), parentTable.getIdTableSchema());
                continue;
            }

            DataProviderVO providerMetadata = representativeControllerZuul.findDataProviderById(providerId);
            String providerCode = "'" + providerMetadata.getCode() + "'";

            List<FieldSchemaVO> parentTableFields = parentTableSchema.getRecordSchema().getFieldSchema();

            StringBuilder preparationSelectClause = new StringBuilder(constructRecordIdCreationForQuery());
            preparationSelectClause.append(", ").append(providerCode).append(" AS ").append(UtilityClass.addQuotesToFieldNames(PARQUET_PROVIDER_CODE_COLUMN_HEADER)).append(", ");

            for (FieldSchemaVO fieldSchema : parentTableFields) {
                if (fieldSchema.getType().equals(DataType.ATTACHMENT)) {
                    preparationSelectClause.append(" '' AS ");
                }
                preparationSelectClause.append(UtilityClass.addQuotesToFieldNames(fieldSchema.getName())).append(", ");
            }

            if (preparationSelectClause.toString().endsWith(", ")) {
                preparationSelectClause = new StringBuilder(preparationSelectClause.substring(0, preparationSelectClause.length() - 2));
            }

           // if the preparation dataset folder exists in S3 that means that we have to delete it to recreate it in the next `CREATE` statement
           if (s3HelperPrivate.checkFolderExist(targetPreparationTableS3FolderPath, S3_PREPARATION_TABLE_NAME_FOLDER_PATH)) {
               LOG.info("Deleting existing table for preparation dataset for datasetId {} and parent table {} to recreate them.", parentDatasetId, parentTable);
               dremioHelperService.demoteFolderOrFile(targetPreparationTableS3FolderPath, parentTableName);
               s3HelperPrivate.deleteFolder(targetPreparationTableS3FolderPath, S3_PREPARATION_TABLE_NAME_FOLDER_PATH);
               LOG.info("Deleted successfully the existing table for preparation dataset for datasetId {} and parent table {} to recreate them.", parentDatasetId, parentTable);
           }

            dropDremioTable(targetPreparationTableDremioQueryPathString);

            StringBuilder queryToCreatePrefilledTable = new StringBuilder("CREATE TABLE " + targetPreparationTableDremioQueryPathString + " AS SELECT " + preparationSelectClause + " FROM " + sourceParentTableDremioQueryPathString);

            if (!dremioHelperService.checkFolderPromoted(sourceParentTableS3FolderPath, sourceParentTableS3FolderPath.getTableName())) {
                //refresh the metadata
                String sourceS3PathForPreparationParquetFolder = s3ServicePrivate.getTableAsFolderQueryPath(sourceParentTableS3FolderPath, S3_TABLE_AS_FOLDER_QUERY_PATH);
                dremioHelperService.refreshTableMetadataAndPromote(null, sourceS3PathForPreparationParquetFolder, sourceParentTableS3FolderPath, sourceParentTableS3FolderPath.getTableName());
            }

            String dremioProcessId = dremioHelperService.executeSqlStatement(String.valueOf(queryToCreatePrefilledTable));
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(String.valueOf(queryToCreatePrefilledTable), dremioProcessId, null);
            dremioHelperService.refreshTableMetadataAndPromote(null, targetPreparationTableDremioQueryPathString, targetPreparationTableDremioQueryPath, parentTableName);
        }
    }

    private void dropDremioTable(String targetPreparationTableDremioQueryPathString) throws Exception {
        String dropQuery = "DROP TABLE IF EXISTS " + targetPreparationTableDremioQueryPathString;
        String dropQueryProcessId = dremioHelperService.executeSqlStatement(dropQuery);
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(String.valueOf(targetPreparationTableDremioQueryPathString), dropQueryProcessId, null);
    }

    private String constructRecordIdCreationForQuery() {
        return "CONCAT(\n" + "        LOWER(LPAD(TO_HEX(CAST(RAND() * 4294967295 AS BIGINT)), 8, '0')), '-',\n" + "        LOWER(LPAD(TO_HEX(CAST(RAND() * 65535 AS BIGINT)), 4, '0')), '-',\n" + "        LOWER(LPAD(TO_HEX(CAST(RAND() * 65535 AS BIGINT)), 4, '0')), '-',\n" + "        LOWER(LPAD(TO_HEX(CAST(RAND() * 65535 AS BIGINT)), 4, '0')), '-',\n" + "        LOWER(LPAD(TO_HEX(CAST(RAND() * 281474976710655 AS BIGINT)), 12, '0'))\n" + "    ) AS " + PARQUET_RECORD_ID_COLUMN_HEADER + " ";
    }

    /**
     * Maps entity to VO.
     */
    private PreparationDatasetVO toVO(PreparationDataset preparationDataset) {

        PreparationDatasetVO vo = new PreparationDatasetVO();
        vo.setId(preparationDataset.getId());
        vo.setParentDatasetId(preparationDataset.getParentDatasetId());
        vo.setDataflowId(preparationDataset.getDataflowId());
        vo.setProviderId(preparationDataset.getProviderId());
        vo.setDatasetName(preparationDataset.getDatasetName());
        vo.setCode(preparationDataset.getCode());
        vo.setIsCreated(preparationDataset.getIsCreated());

        return vo;
    }
}
