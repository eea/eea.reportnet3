export const IntegrationConfig = {
  all: '/integration/listIntegrations',
  allExtensionsOperations: '/integration/listExtensionsOperations',
  create: '/integration/create',
  delete: '/integration/{:integrationId}/dataflow/{:dataflowId}',
  euDatasetIntegration: '/integration/findExportEUDatasetIntegration?datasetSchemaId={:datasetSchemaId}',
  getProcesses: '/fme/findItems?datasetId={:datasetId}&repository={:repositoryName}',
  getRepositories: '/fme/findRepositories?datasetId={:datasetId}',
  runIntegration: '/integration/{:integrationId}/runIntegration/dataset/{:datasetId}',
  runIntegrationWithReplace: '/integration/{:integrationId}/runIntegration/dataset/{:datasetId}?replace={:replaceData}',
  update: '/integration/update'
};
