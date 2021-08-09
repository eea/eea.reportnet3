export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadShowValidationsFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  generateShowValidationsFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}'
};
