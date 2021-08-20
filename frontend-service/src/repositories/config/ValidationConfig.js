export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadQCRulesFile: '/rules/downloadFile/{:datasetId}?fileName={:fileName}',
  downloadShowValidationsFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  generateQCRulesFile: '/rules/export/{:datasetId}', // TODO INTEGRATION WITH BACKEND
  generateShowValidationsFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}'
};
