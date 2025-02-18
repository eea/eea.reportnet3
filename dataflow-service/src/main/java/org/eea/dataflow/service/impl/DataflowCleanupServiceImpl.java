package org.eea.dataflow.service.impl;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.*;
import org.eea.dataflow.service.DataflowCleanupService;
import org.eea.exception.EEAException;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class DataflowCleanupServiceImpl implements DataflowCleanupService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DataflowCleanupServiceImpl.class);

    /** The dataset metabase controller. */
    @Autowired
    private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

    /** The resource management controller zull. */
    @Autowired
    private ResourceManagementControllerZull resourceManagementControllerZull;

    /** The resource management controller zull. */
    @Autowired
    private RulesControllerZuul rulesControllerZuul;

    /** The dataset schema controller zuul. */
    @Autowired
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

    /** The dataset controller zuul. */
    @Autowired
    private DataSetControllerZuul     dataSetControllerZuul;

    /** The dataflow repository. */
    @Autowired
    private DataflowRepository dataflowRepository;

    @Autowired
    private DataflowCleanupRepository dataflowCleanupRepository;

    @Autowired
    private DataflowServiceImpl dataflowService;

    /** The timeout. */
    @Value("${scheduling.jobForDeletingSoftDeletedDataflows.numberOfMonths}")
    private Integer numberOfMonths;

    @Transactional
    @Override
    public void deleteDataflowsOlderThanNumberOfMonths() throws EEAException {
        LOG.info("Starting cleanup process for soft-deleted dataflows older than {} months.", numberOfMonths);

        List<Dataflow> dataflowsToDelete;
        try {
            dataflowsToDelete = dataflowRepository.findSoftDeletedDataflowsOlderThanNumberOfMonths(numberOfMonths);
        } catch (Exception e) {
            LOG.error("Error retrieving dataflows for deletion older than {} months.", numberOfMonths, e);
            throw new EEAException(EEAErrorMessage.ERROR_RETRIEVING_SOFT_DELETED_DATAFLOWS, e);
        }

        if (dataflowsToDelete == null || dataflowsToDelete.isEmpty()) {
            LOG.info("Daily Cleanup No dataflows found for deletion.");
            return;
        }

        List<Long> dataflowIds = new ArrayList<>();
        for (Dataflow dataflowVO : dataflowsToDelete) {
            try {
                Long dataflowId = dataflowVO.getId();
                cleanupDataflow(dataflowId);
                dataflowIds.add(dataflowId);
            } catch (EEAException e) {
                LOG.error("{}: {}", EEAErrorMessage.ERROR_CLEANUP_SOFT_DELETED_DATAFLOW, dataflowVO.getId(), e);
            } catch (Exception e) {
                LOG.error("Unexpected error while deleting dataflow with ID: {}", dataflowVO.getId(), e);
                throw new EEAException(EEAErrorMessage.ERROR_CLEANUP_SOFT_DELETED_DATAFLOW, e);
            }
        }
        LOG.info("Daily Cleanup process completed for dataflowIds {}", dataflowIds);
    }

    @Override
    public void cleanupDataflowBigData(Long dataflowId) {
        dataSetControllerZuul.deleteBigDataRootFolder(dataflowId);
    }

    @Transactional
    @Override
    public void cleanupDataflow(Long dataflowId) throws EEAException {
        LOG.info("Processing deletion for dataflow ID: {}", dataflowId);
        try {
            // Step 1:
            // Retrieve dataset schemas and Dataset ids to be used for mongo deletions
            Map<Long,String> datasetSchemasIdsAndDatasetIds = retrieveDatasetSchemasAndDatasetIdsForDataflow(dataflowId);
            // Retrieve datasetIdsAndGroups to be used for Keyclock group deletions
            Map<Long, String> datasetIdsAndGroups = retrieveDatasetIdsGroupsForDataflow(dataflowId);

            // Step 2: Delete dataflow Keycloak resources
            if (!datasetIdsAndGroups.isEmpty()) {
                deleteDataflowResourcesReporting(datasetIdsAndGroups, dataflowId);
            } else {
                LOG.warn("No dataset groups found for deletion for Keyclock groups for dataflow ID: {}", dataflowId);
            }

            // Step 3: Delete dependent MongoDB records.
            datasetSchemasIdsAndDatasetIds.forEach((datasetId, datasetSchemaId) -> {
                try {
                    deleteRulesSchemaFromMongo(datasetId, datasetSchemaId );
                    deleteDatasetSchemaPKCatalogueFromMongo(datasetId, datasetSchemaId);
                    deleteDatasetSchemaUniqueConstrainsFromMongo(datasetId, datasetSchemaId);
                    deleteDatasetSchemaFromMongo(datasetId, datasetSchemaId);
                } catch (EEAException e) {
                    throw new RuntimeException(e);
                }
            });

            // Step 4: For big data flows, perform additional Datalakes cleanup BEFORE deleting the metabase record
            boolean isBigData = dataflowService.getMetabaseById(dataflowId).getBigData();
            if (isBigData) {
                cleanupDataflowBigData(dataflowId);
            }

            // Step 5: Delete Metabase records;
            executeMetabaseDataflowRelatedObjectDeletions(dataflowId);

            LOG.info("Completed cleanup orchestration for dataflow ID: {}", dataflowId);
        }
        catch (Exception e) {
            LOG.error("Exception encountered during cleanupDataflow process for ID: {}", dataflowId, e);
            throw new EEAException(EEAErrorMessage.ERROR_CLEANUP_SOFT_DELETED_DATAFLOW, e);
        }
    }

   @Override
   public List<String> retrieveDatasetSchemasForDataflow(Long dataflowId) {
        List<DataSetMetabaseVO> datasetsVO = datasetMetabaseControllerZuul.getAllDatasetsByDataflowId(dataflowId);
        List<String> datasetSchemas = datasetsVO.stream()
                .map(DataSetMetabaseVO::getDatasetSchema)
                .filter(Objects::nonNull) // Avoid NullPointerException
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
        return datasetSchemas;
    }

    public Map<Long, String> retrieveDatasetSchemasAndDatasetIdsForDataflow(Long dataflowId) {
        List<DataSetMetabaseVO> datasetsVO = datasetMetabaseControllerZuul.getAllDatasetsByDataflowId(dataflowId);
        return datasetsVO.stream()
                .filter(vo -> vo.getDatasetSchema() != null) // Avoid null schemas
                .collect(Collectors.toMap(
                        DataSetMetabaseVO::getId,         // Key: Dataset ID
                        DataSetMetabaseVO::getDatasetSchema // Value: Dataset Schema ID
                ));
    }

    @Override
    public Map<Long, String> retrieveDatasetIdsGroupsForDataflow(Long dataflowId) {
        return datasetMetabaseControllerZuul.getDatasetIdsAndGroups(dataflowId);
    }

    @Override
    public void deleteDataflowResourcesReporting(Map<Long, String> datasetIdsAndGroups, Long dataflowId) throws EEAException {
        LOG.info("Executing Keycloak group deletions for dataset groups.");
        try {
            List<Long> datasetIds = new ArrayList<>(datasetIdsAndGroups.keySet());
            try {
                List<ResourceInfoVO> resourceCustodian = resourceManagementControllerZull
                        .getGroupsByIdResourceTypePrivate(dataflowId, ResourceTypeEnum.DATAFLOW);

                resourceManagementControllerZull.deleteResourcePrivate(resourceCustodian);

            } catch (Exception e) {
                LOG.error("Error deleting resources in Keycloak, group with the id: {}, and exception {}", dataflowId, e.getMessage());
                throw new EEAException(EEAErrorMessage.ERROR_DELETING_KEYCLOAK_GROUPS, e);
            }

            for (Long datasetId : datasetIds) {
                try {
                    resourceManagementControllerZull.deleteResourceByDatasetIdPrivate(datasetId);
                    TimeUnit.MILLISECONDS.sleep(1000); // Delay of 1000ms to prevent timeouts
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Thread interrupted while deleting dataset resource ID: {}", datasetId);
                } catch (Exception e) {
                    LOG.error("Failed to delete dataset resource ID: {}", datasetId, e);
                    throw new EEAException(EEAErrorMessage.ERROR_DELETING_KEYCLOAK_GROUPS, e);
                }
            }

        } catch (EEAException e) {
            LOG.error("EEAException encountered during Keycloak deletion for Dataflow ID: {}", dataflowId, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Failed to delete Keycloak groups: {}", datasetIdsAndGroups.keySet(), e);
            throw new EEAException(EEAErrorMessage.ERROR_DELETING_KEYCLOAK_GROUPS, e);
        }
    }

    @Transactional
    @Override
    public void executeMetabaseDataflowRelatedObjectDeletions(Long dataflowId) throws EEAException {
        LOG.info("Executing Metabase deletion queries for dataflow ID: {}", dataflowId);
        try {
            dataflowCleanupRepository.deleteSnapshots(dataflowId);
            dataflowCleanupRepository.deleteSnapshotSchemas(dataflowId);
            dataflowCleanupRepository.deleteEuDatasets(dataflowId);
            dataflowCleanupRepository.deleteDataCollections(dataflowId);
            dataflowCleanupRepository.deleteReportingDatasets(dataflowId);
            dataflowCleanupRepository.deletePartitionDatasets(dataflowId);
            dataflowCleanupRepository.deleteTestDatasets(dataflowId);
            dataflowCleanupRepository.deleteDesignDatasets(dataflowId);
            dataflowCleanupRepository.deleteReferenceDatasets(dataflowId);
            dataflowCleanupRepository.deleteStatistics(dataflowId);
            dataflowCleanupRepository.deleteWeblinks(dataflowId);
            dataflowCleanupRepository.deleteDocuments(dataflowId);
            dataflowCleanupRepository.deleteIntegrationOperationParams(dataflowId);
            dataflowCleanupRepository.deleteIntegrations(dataflowId);
            dataflowCleanupRepository.deleteRepresentatives(dataflowId);
            dataflowCleanupRepository.deleteForeignRelationsOrigin(dataflowId);
            dataflowCleanupRepository.deleteForeignRelationsDestination(dataflowId);
            dataflowCleanupRepository.deleteDatasets(dataflowId);
            dataflowCleanupRepository.deleteNativeDataflow(dataflowId);
            LOG.info("Successfully executed Metabase deletion queries for dataflow ID: {}", dataflowId);
        } catch (Exception e) {
            LOG.error("Error during execution of Metabase deletion steps for dataflow ID: {}", dataflowId, e);
            throw new EEAException(EEAErrorMessage.ERROR_DELETING_DATAFLOW_METABASE_RELATED_OBJECTS, e);
        }
    }

    @Override
    public void deleteRulesSchemaFromMongo(Long datasetId, String datasetSchemaId) throws EEAException {
        try {
            rulesControllerZuul.deleteRulesSchema(datasetSchemaId, datasetId);
        } catch (Exception e) {
            LOG.error("Error while deleting RulesSchema from MongoDB for dataset ID: {}", datasetId, e);
            throw new EEAException(EEAErrorMessage.ERROR_DELETING_RULES_SCHEMA, e);
        }
    }

    @Override
    public void deleteDatasetSchemaFromMongo(Long datasetId, String datasetSchemaId) throws EEAException {
        try {
                datasetSchemaControllerZuul.deleteDatasetSchemaRulesAndIntegrityPrivate(datasetSchemaId, datasetId);
        } catch (Exception e) {
            LOG.error("Error while deleting DataSetSchema from MongoDB for dataset ID: {}", datasetId, e);
            throw new EEAException(EEAErrorMessage.ERROR_DELETING_DATASET_SCHEMA, e);
        }
    }

    @Override
    public void deleteDatasetSchemaPKCatalogueFromMongo(Long datasetId, String datasetSchemaId) throws EEAException {
        try {
            datasetSchemaControllerZuul.deleteDatasetSchemaPKCataloguePrivate(datasetSchemaId, datasetId);
        } catch (Exception e) {
            LOG.error("Error while deleting PK Catalogue from MongoDB for dataset ID: {}", datasetId, e);
            throw new EEAException(EEAErrorMessage.ERROR_DELETING_DATASET_SCHEMA, e);
        }
    }

    @Override
    public void deleteDatasetSchemaUniqueConstrainsFromMongo(Long datasetId, String datasetSchemaId) throws EEAException {
        try {
            datasetSchemaControllerZuul.deleteUniqueConstrainsPrivate(datasetSchemaId);
        } catch (Exception e) {
            LOG.error("Error while deleting PK Catalogue from MongoDB for dataset ID: {}", datasetId, e);
            throw new EEAException(EEAErrorMessage.ERROR_DELETING_UNIQUE_CONSTRAINS, e);
        }
    }
}
