import isUndefined from 'lodash/isUndefined';

import { IntegrationConfig } from './config/IntegrationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const IntegrationRepository = {
  all: async integration =>
    await HTTPRequester.update({
      url: getUrl(IntegrationConfig.all),
      data: integration
    }),

  allExtensionsOperations: async integration =>
    await HTTPRequester.update({
      url: getUrl(IntegrationConfig.allExtensionsOperations),
      data: integration
    }),

  create: async integration =>
    await HTTPRequester.post({
      url: getUrl(IntegrationConfig.create),
      data: integration
    }),

  deleteById: async (dataflowId, integrationId) =>
    await HTTPRequester.delete({
      url: getUrl(IntegrationConfig.delete, { dataflowId, integrationId })
    }),

  findEUDatasetIntegration: async datasetSchemaId =>
    await HTTPRequester.get({
      url: getUrl(IntegrationConfig.euDatasetIntegration, { datasetSchemaId })
    }),

  getProcesses: async (repositoryName, datasetId) =>
    await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getProcesses, { datasetId, repositoryName })
    }),
  getRepositories: async datasetId =>
    await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getRepositories, { datasetId })
    }),
  runIntegration: async (integrationId, datasetId, replaceData) => {
    if (isUndefined(replaceData)) {
      return await HTTPRequester.post({
        url: getUrl(IntegrationConfig.runIntegration, { integrationId, datasetId })
      });
    } else {
      return await HTTPRequester.post({
        url: getUrl(IntegrationConfig.runIntegrationWithReplace, { integrationId, datasetId, replaceData })
      });
    }
  },

  update: async integration =>
    await HTTPRequester.update({
      url: getUrl(IntegrationConfig.update),
      data: integration
    })
};
