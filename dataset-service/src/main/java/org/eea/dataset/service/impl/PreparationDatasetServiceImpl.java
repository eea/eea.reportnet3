package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
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
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.eea.utils.LiteralConstants.*;

/**
 * The Class PreparationDatasetServiceImpl.
 */
@Service
public class PreparationDatasetServiceImpl implements PreparationDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(PreparationDatasetServiceImpl.class);

    private final PreparationDatasetRepository preparationDatasetRepository;
    private final DremioHelperService dremioHelperService;
    private final DatasetMetabaseService datasetMetabaseService;
    private final DatasetSchemaService datasetSchemaService;
    private final S3Service s3ServicePrivate;
    private final S3Helper s3HelperPrivate;
    private final RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;
    private final KafkaSenderUtils kafkaSenderUtils;
    private final PreparationDatasetMapper preparationDatasetMapper;

    @Autowired
    public PreparationDatasetServiceImpl(PreparationDatasetRepository preparationDatasetRepository,
                                         DremioHelperService dremioHelperService,
                                         DatasetMetabaseService datasetMetabaseService,
                                         DatasetSchemaService datasetSchemaService,
                                         S3Helper s3HelperPrivate,
                                         RepresentativeController.RepresentativeControllerZuul representativeControllerZuul,
                                         KafkaSenderUtils kafkaSenderUtils,
                                         PreparationDatasetMapper preparationDatasetMapper) {
        this.preparationDatasetRepository = preparationDatasetRepository;
        this.dremioHelperService = dremioHelperService;
        this.datasetMetabaseService = datasetMetabaseService;
        this.datasetSchemaService = datasetSchemaService;
        this.s3HelperPrivate = s3HelperPrivate;
        this.s3ServicePrivate = s3HelperPrivate.getS3Service();
        this.representativeControllerZuul = representativeControllerZuul;
        this.kafkaSenderUtils = kafkaSenderUtils;
        this.preparationDatasetMapper = preparationDatasetMapper;
    }

    @Override
    @Transactional
    public List<PreparationDatasetVO> findPreparationDatasets(
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

        return entities.stream().map(this::toVO).collect(Collectors.toList());
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
    public void deletePreparationDatasetById(Long preparationId) throws EEAException {

        if (!preparationDatasetRepository.existsById(preparationId)) {
            throw new EEAException("Preparation dataset not found");
        }

        preparationDatasetRepository.deleteById(preparationId);
    }

    @Override
    @Transactional
    public void createAllEligiblePreparationSets(Long dataflowId, Long providerId) throws EEAException {
        LOG.info("Creating preparation sets for dataflow {} and providerId {}", dataflowId, providerId);
        NotificationVO notificationVO = NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .dataflowId(dataflowId)
                .providerId(providerId)
                .build();

        // fetch all preparations datasets that haven't yet been created, `isCreated=false`
        List<PreparationDatasetVO> preparationDatasetVOS = this.findByDataflowIdAndProviderIdAndIsCreated(dataflowId, providerId, false);
        if (preparationDatasetVOS.isEmpty()) {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_HAS_EMPTY_QUEUE_EVENT,null, notificationVO);
            LOG.info("No preparation dataset found without isCreated=FALSE for dataflow {} and providerId {}", dataflowId, providerId);
            return;
        }
        LOG.info("Creating {} preparation datasets for dataflow {} and providerId {}", preparationDatasetVOS.size(), dataflowId, providerId);

        List<DataSetMetabaseVO> datasetMetabaseVOS = datasetMetabaseService.getDatasetsByDataflowIdAndProviderId(dataflowId, providerId);

        // for each `preparation_dataset` record that has `is_created=false` and for all the dataset ids that need to be created within it
        for (PreparationDatasetVO preparationDatasetVO : preparationDatasetVOS) {
            datasetMetabaseVOS.forEach(datasetMetabaseVO -> {
                try {
                    LOG.info("Copying tables from parent dataset to preparation dataset for parent datasetId {} and code {} ", datasetMetabaseVO.getId(), preparationDatasetVO.getCode());
                    copyParentDatasetDataToPreparationDataset(datasetMetabaseVO, preparationDatasetVO.getCode());
                    LOG.info("Successfully copied tables from parent dataset to preparation dataset for parent datasetId {} and code {} ", datasetMetabaseVO.getId(), preparationDatasetVO.getCode());
                } catch (Exception e) {
                    LOG.error("Creation of preparation dataset failed for datasetId {} and code {}", datasetMetabaseVO.getId(), preparationDatasetVO.getCode(), e);
                    try { kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_FAILED_EVENT,null, notificationVO);
                    } catch (EEAException ex) { throw new RuntimeException(ex); }
                    throw new RuntimeException(e);
                }
            });
            LOG.info("Creation of preparation dataset with code {} completed.", preparationDatasetVO.getCode());
            preparationDatasetVO.setIsCreated(Boolean.TRUE);
            preparationDatasetRepository.save(preparationDatasetMapper.classToEntity(preparationDatasetVO));
        }

        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.PREPARATION_DATASET_CREATION_COMPLETED_EVENT,null, notificationVO);
        LOG.info("Successfully created preparation sets for dataflow {} and providerId {} for all datasets", dataflowId, providerId);
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
                LOG.info("No tables to copy for datasetId {} and parent table {}, skipping this dataset.", parentDatasetId, parentTable);
                continue;
            }

            String providerCode = "''";
            if (providerId != 0L) {
                DataProviderVO providerMetadata = representativeControllerZuul.findDataProviderById(providerId);
                providerCode = "'" + providerMetadata.getCode() + "'";
            }

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
