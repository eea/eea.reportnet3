package org.eea.dataflow.service;

import org.eea.exception.EEAException;

import java.util.List;
import java.util.Map;


public interface DataflowCleanupService {

    void deleteDataflowsOlderThanNumberOfMonths() throws EEAException;

    void cleanupDataflow(Long dataflowId) throws EEAException;

    List<String> retrieveDatasetSchemasForDataflow(Long dataflowId);

    Map<Long, String> retrieveDatasetIdsGroupsForDataflow(Long dataflowId);

    void deleteDataflowResourcesReporting(Map<Long, String> datasetIdsAndGroups, Long dataflowId) throws EEAException;

    void executeMetabaseDataflowRelatedObjectDeletions(Long dataflowId) throws EEAException;

    void deleteRulesSchemaFromMongo(Long datasetId, String datasetSchemaId) throws EEAException;

    void deleteDatasetSchemaFromMongo(Long datasetId, String datasetSchemaId) throws EEAException;

    void deleteDatasetSchemaPKCatalogueFromMongo(Long datasetId, String datasetSchemaId) throws EEAException;

    void deleteDatasetSchemaUniqueConstrainsFromMongo(Long datasetId, String datasetSchemaId) throws EEAException;

    void cleanupDataflowBigData(Long dataflowId);

}
