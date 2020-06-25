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

  deleteById: async integrationId => {
    const response = await HTTPRequester.delete({
      url: getUrl(IntegrationConfig.delete, { integrationId })
    });
    return response;
  },

  update: async integration => {
    const response = await HTTPRequester.update({
      url: getUrl(IntegrationConfig.update),
      data: integration
    });
    return response;
  }
};
