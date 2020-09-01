import isUndefined from 'lodash/isUndefined';

import { IntegrationConfig } from 'conf/domain/model/Integration';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiIntegration = {
  all: async integration => {
    const response = await HTTPRequester.update({
      url: getUrl(IntegrationConfig.all),
      data: integration
    });

    return response.data;
  },

  allExtensionsOperations: async integration => {
    const response = await HTTPRequester.update({
      url: getUrl(IntegrationConfig.allExtensionsOperations),
      data: integration
    });

    return response.data;
  },

  create: async integration => {
    const response = await HTTPRequester.post({
      url: getUrl(IntegrationConfig.create),
      data: integration
    });

    return response;
  },

  deleteById: async (dataflowId, integrationId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(IntegrationConfig.delete, { dataflowId, integrationId })
    });

    return response;
  },

  findEUDatasetIntegration: async datasetSchemaId => {
    const response = await HTTPRequester.get({
      url: getUrl(IntegrationConfig.euDatasetIntegration, { datasetSchemaId })
    });

    return response.data;
  },

  getProcesses: async (repositoryName, datasetId) => {
    const response = await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getProcesses, { datasetId, repositoryName })
    });

    return response.data;
  },

  getRepositories: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getRepositories, { datasetId })
    });

    return response.data;
  },

  runIntegration: async (integrationId, datasetId, replaceData) => {
    if (isUndefined(replaceData)) {
      const response = await HTTPRequester.post({
        url: getUrl(IntegrationConfig.runIntegration, { integrationId, datasetId })
      });

      return response.data;
    } else {
      const response = await HTTPRequester.post({
        url: getUrl(IntegrationConfig.runIntegrationWithReplace, { integrationId, datasetId, replaceData })
      });

      return response.data;
    }
  },

  update: async integration => {
    const response = await HTTPRequester.update({
      url: getUrl(IntegrationConfig.update),
      data: integration
    });

    return response;
  }
};
