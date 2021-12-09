export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadQCRulesFile: '/rules/downloadQC/{:datasetId}?fileName={:fileName}',
  downloadShowValidationsFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  generateQCRulesFile: '/rules/exportQC/{:datasetId}',
  generateShowValidationsFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}/dataflow/{:dataflowId}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}',
  validateSqlSentence: 'rules/evaluateSqlRule'
};
