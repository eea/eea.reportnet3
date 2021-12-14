export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadQCRulesFile: '/rules/downloadQC/{:datasetId}?fileName={:fileName}',
  downloadShowValidationsFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  generateQCRulesFile: '/rules/exportQC/{:datasetId}',
  generateShowValidationsFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}/dataflow/{:dataflowId}',
  runSqlRule:
    '/rules/runSqlRule?datasetId={:datasetId}&sqlRule={:sqlSentence}&showInternalFields={:showInternalFields}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}',
  evaluateSqlSentence: '/rules/evaluateSqlRule?datasetId={:datasetId}&sqlRule={:sqlSentence}'
};
