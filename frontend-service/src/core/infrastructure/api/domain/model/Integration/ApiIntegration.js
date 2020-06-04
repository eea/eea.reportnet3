import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';
import { userStorage } from 'core/domain/model/User/UserStorage';

const data = {
  status: 200,
  data: {
    list: [
      {
        datasetSchemaId: 777,
        externalTool: 'External first tool',
        externalUrl: 'www.Integration.com',
        fileExtension: 'csv',
        integrationId: '001',
        integrationName: 'First Integration',
        operation: 'import',
        parameters: { parameter1: 'parameter1', parameter2: 'parameter2' }
      },
      {
        datasetSchemaId: 777,
        externalTool: 'External second tool',
        externalUrl: 'www.Integration.com',
        fileExtension: 'xlsx',
        integrationId: '002',
        integrationName: 'Second Integration',
        operation: 'export',
        parameters: { parameter1: 'parameter1', parameter2: 'parameter2' }
      }
    ]
  }
};

export const apiIntegration = {
  all: async () => {
    const tokens = userStorage.get();
    const response = await data;
    return response.data;
  }
};
