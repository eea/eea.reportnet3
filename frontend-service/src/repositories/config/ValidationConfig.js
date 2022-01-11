export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadQCRulesFile: '/rules/downloadQC/{:datasetId}?fileName={:fileName}',
  downloadShowValidationsFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  evaluateSqlSentence: '/rules/evaluateSqlRule?datasetId={:datasetId}',
  generateQCRulesFile: '/rules/exportQC/{:datasetId}',
  generateShowValidationsFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}/dataflow/{:dataflowId}',
  getAllQCsHistoricInfo: '/rules/historicDatasetRules?datasetId={:datasetId}',
  getQcHistoricInfo: '/rules/historicInfo?datasetId={:datasetId}&ruleId={:ruleId}',
  runSqlRule: '/rules/runSqlRule?datasetId={:datasetId}&showInternalFields={:showInternalFields}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}',
  viewUpdated: '/dataset/{:datasetId}/viewUpdated'
};
