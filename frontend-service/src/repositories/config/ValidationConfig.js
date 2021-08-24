export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadQCRulesFile: '/rules/downloadFile/{:datasetId}?fileName={:fileName}', // TODO INTEGRATION WITH BACKEND
  downloadShowValidationsFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  generateQCRulesFile: '/rules/exportQC/{:datasetId}',
  generateShowValidationsFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}'
};
