export const ValidationConfig = {
  create: '/rules/createNewRule?datasetId={:datasetId}',
  delete: '/rules/deleteRule?datasetId={:datasetSchemaId}&ruleId={:ruleId}',
  downloadFile: '/validation/downloadFile/{:datasetId}?fileName={:fileName}',
  generateFile: '/validation/export/{:datasetId}',
  getAll: '/rules/{:datasetSchemaId}',
  update: '/rules/updateRule?datasetId={:datasetId}',
  updateAutomatic: '/rules/updateAutomaticRule/{:datasetId}'
};
