export const IntegrationConfig = {
  getAll: '/integration/listIntegrations',
  getAllExtensionsOperations: '/integration/listExtensionsOperations',
  getEUDatasetIntegration:
    '/integration/findExportEUDatasetIntegration?dataflowId={:dataflowId}&datasetSchemaId={:datasetSchemaId}',
  getFMEProcesses: '/fme/findItems?datasetId={:datasetId}&repository={:repositoryName}',
  getFMERepositories: '/fme/findRepositories?datasetId={:datasetId}',
  create: '/integration/create',
  delete: '/integration/{:integrationId}/dataflow/{:dataflowId}',
  runIntegration: '/integration/{:integrationId}/runIntegration/dataset/{:datasetId}',
  runIntegrationWithReplace: '/integration/{:integrationId}/runIntegration/dataset/{:datasetId}?replace={:replaceData}',
  update: '/integration/update'
};
