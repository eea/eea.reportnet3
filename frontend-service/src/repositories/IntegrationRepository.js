import isUndefined from 'lodash/isUndefined';

import { IntegrationConfig } from './config/IntegrationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const IntegrationRepository = {
  getAll: async integration => await HTTPRequester.update({ url: getUrl(IntegrationConfig.getAll), data: integration }),

  getAllExtensionsOperations: async integration =>
    await HTTPRequester.update({ url: getUrl(IntegrationConfig.getAllExtensionsOperations), data: integration }),

  create: async integration => await HTTPRequester.post({ url: getUrl(IntegrationConfig.create), data: integration }),

  delete: async (dataflowId, integrationId) =>
    await HTTPRequester.delete({ url: getUrl(IntegrationConfig.delete, { dataflowId, integrationId }) }),

  getEUDatasetIntegration: async (dataflowId, datasetSchemaId) =>
    await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getEUDatasetIntegration, { dataflowId, datasetSchemaId })
    }),

  getFMEProcesses: async (repositoryName, datasetId) =>
    await HTTPRequester.get({ url: getUrl(IntegrationConfig.getFMEProcesses, { datasetId, repositoryName }) }),

  getFMERepositories: async datasetId =>
    await HTTPRequester.get({ url: getUrl(IntegrationConfig.getFMERepositories, { datasetId }) }),

  runIntegration: async (integrationId, datasetId, replaceData) => {
    if (isUndefined(replaceData)) {
      return await HTTPRequester.post({ url: getUrl(IntegrationConfig.runIntegration, { integrationId, datasetId }) });
    } else {
      return await HTTPRequester.post({
        url: getUrl(IntegrationConfig.runIntegrationWithReplace, { integrationId, datasetId, replaceData })
      });
    }
  },

  update: async integration => await HTTPRequester.update({ url: getUrl(IntegrationConfig.update), data: integration })
};
