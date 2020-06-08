import { IntegrationConfig } from 'conf/domain/model/Integration';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

const data = {
  status: 200,
  data: {
    list: [
      {
        externalParameters: { parameter1: 'parameter1', parameter2: 'parameter2', parameter3: 'parameter3' },
        externalTool: 'External first tool',
        integrationDescription: 'This is the first description',
        integrationId: '001',
        integrationName: 'First Integration',
        internalParameters: {
          datasetSchemaId: 777,
          fileExtension: 'csv'
        },
        operation: 'import'
      },
      {
        externalParameters: { parameter1: 'parameter1', parameter2: 'parameter2', parameter3: 'parameter3' },
        externalTool: 'External second tool',
        integrationDescription: 'This is a description',
        integrationId: '002',
        integrationName: 'Second Integration',
        internalParameters: {
          datasetSchemaId: 777,
          fileExtension: 'json'
        },
        operation: 'export'
      }
    ]
  }
};

export const apiIntegration = {
  all: async () => {
    const tokens = userStorage.get();
    const response = await data;
    return response.data;
  },

  create: async integration => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(IntegrationConfig.create),
      data: integration,
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  },

  deleteById: async integrationId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(IntegrationConfig.delete, { integrationId }),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  }
};
