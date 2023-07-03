package org.eea.validation.service;

public interface DremioRulesExecuteService {

    void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception;

}
