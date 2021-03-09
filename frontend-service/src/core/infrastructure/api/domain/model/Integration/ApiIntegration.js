import isUndefined from 'lodash/isUndefined';

import { IntegrationConfig } from 'conf/domain/model/Integration';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiIntegration = {
  all: async integration => {
    return await HTTPRequester.update({
      url: getUrl(IntegrationConfig.all),
      data: integration
    });
  },

  allExtensionsOperations: async integration => {
    return await HTTPRequester.update({
      url: getUrl(IntegrationConfig.allExtensionsOperations),
      data: integration
    });
  },

  create: async integration => {
    return await HTTPRequester.post({
      url: getUrl(IntegrationConfig.create),
      data: integration
    });
  },

  deleteById: async (dataflowId, integrationId) => {
    return await HTTPRequester.delete({
      url: getUrl(IntegrationConfig.delete, { dataflowId, integrationId })
    });
  },

  findEUDatasetIntegration: async datasetSchemaId => {
    return await HTTPRequester.get({
      url: getUrl(IntegrationConfig.euDatasetIntegration, { datasetSchemaId })
    });
  },

  getProcesses: async (repositoryName, datasetId) => {
    return await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getProcesses, { datasetId, repositoryName })
    });
  },

  getRepositories: async datasetId => {
    return await HTTPRequester.get({
      url: getUrl(IntegrationConfig.getRepositories, { datasetId })
    });
  },

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

  update: async integration => {
    return await HTTPRequester.update({
      url: getUrl(IntegrationConfig.update),
      data: integration
    });
  }
};
