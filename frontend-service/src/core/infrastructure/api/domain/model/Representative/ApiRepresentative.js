import { RepresentativeConfig } from 'conf/domain/model/Representative';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

const apiRepresentative = {
  allRepresentatives: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.all, {
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
      { label: 'United Kingdom', dataProviderId: 2 },
      { label: 'Germany', dataProviderId: 3 },
      { label: 'Spain', dataProviderId: 1 },
      { label: 'France', dataProviderId: 5 },
      { label: 'Italy', dataProviderId: 6 }
    ];

    let result = hardcodedResponseExample;

    return result;
  },

  add: (dataflowId, providerAccount, dataProviderId) => {
    console.log(
      'Adding Representative to dataflowId: ',
      dataflowId,
      ' providerAccount:',
      providerAccount,
      ' dataProviderId:',
      dataProviderId
    );
  },

  deleteById: (dataflowId, representativeId) => {
    console.log('Deliting Representative from dataflowId: ', dataflowId, ' representativeId', representativeId);
  },

  update: (dataflowId, representativeId, providerAccount, dataProviderId) => {
    console.log(
      `Updating Representative from dataflowId: ${dataflowId}, dataProvider Id: ${representativeId}, new Role: ${(providerAccount,
      dataProviderId)}`
    );
  }
};

export { apiRepresentative };
