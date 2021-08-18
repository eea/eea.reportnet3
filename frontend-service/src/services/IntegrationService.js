import { IntegrationRepository } from 'repositories/IntegrationRepository';

import { IntegrationUtils } from 'services/_utils/IntegrationUtils';

export const IntegrationService = {
  getAll: async (dataflowId, datasetSchemaId) => {
    const integrationsDTO = await IntegrationRepository.getAll(
      IntegrationUtils.parseDatasetSchemaId(datasetSchemaId, dataflowId)
    );
    return IntegrationUtils.parseIntegrationsList(integrationsDTO.data);
  },

  getAllExtensionsOperations: async (dataflowId, datasetSchemaId) => {
    const integrationsDTO = await IntegrationRepository.getAllExtensionsOperations(
      IntegrationUtils.parseDatasetSchemaId(datasetSchemaId, dataflowId)
    );
    return IntegrationUtils.parseIntegrationsOperationsExtensionsList(integrationsDTO.data);
  },

  getEUDatasetIntegration: async (dataflowId, datasetSchemaId) => {
    const eUDatasetIntegrationsDTO = await IntegrationRepository.getEUDatasetIntegration(dataflowId, datasetSchemaId);
    return IntegrationUtils.parseIntegration(eUDatasetIntegrationsDTO.data);
  },

  getFMEProcesses: async (repositoryName, datasetId) => {
    const processes = await IntegrationRepository.getFMEProcesses(repositoryName, datasetId);
    return IntegrationUtils.parseProcessList(processes.data);
  },

  getFMERepositories: async datasetId => {
    const repositories = await IntegrationRepository.getFMERepositories(datasetId);
    return IntegrationUtils.parseRepositoryList(repositories.data);
  },

  create: async integration => IntegrationRepository.create(IntegrationUtils.parseManageIntegration(integration)),

  delete: async (dataflowId, integrationId) => await IntegrationRepository.delete(dataflowId, integrationId),

  update: async integration => IntegrationRepository.update(IntegrationUtils.parseManageIntegration(integration)),

  runIntegration: async (integrationId, datasetId, replaceData) =>
    await IntegrationRepository.runIntegration(integrationId, datasetId, replaceData)
};
