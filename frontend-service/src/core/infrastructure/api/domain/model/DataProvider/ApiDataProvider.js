import { DataProviderConfig } from 'conf/domain/model/DataProvider';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

const apiDataProvider = {
  allRepresentatives: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataProviderConfig.all, {
        dataflowId: dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },

  allDataProviders: async type => {
    let hardcodedResponseExample = [
      { dataProviderId: '1111', name: 'Es' },
      { dataProviderId: '2222', name: 'De' },
      { dataProviderId: '3333', name: 'UK' },
      { dataProviderId: '4444', name: 'Fr' },
      { dataProviderId: '5555', name: 'It' }
    ];

    let result = hardcodedResponseExample;

    return result;
  },

  add: (dataflowId, dataProviderEmail, dataProviderName) => {
    console.log(
      'Adding DataProvider to dataflowId: ',
      dataflowId,
      ' dataProviderEmail:',
      dataProviderEmail,
      ' name:',
      dataProviderName
    );
  },

  deleteById: (dataflowId, dataProviderId) => {
    console.log('Deliting DataProvider from dataflowId: ', dataflowId, ' dataProviderId', dataProviderId);
  },

  update: (dataflowId, dataProviderId, dataProviderEmail, dataProviderName) => {
    console.log(
      `Updating DataProvider from dataflowId: ${dataflowId}, dataProvider Id: ${dataProviderId}, new Role: ${(dataProviderEmail,
      dataProviderName)}`
    );
  }
};

export { apiDataProvider };
