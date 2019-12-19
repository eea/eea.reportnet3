import { RepresentativeConfig } from 'conf/domain/model/Representative';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

const apiRepresentative = {
  add: async (dataflowId, providerAccount, dataProviderId) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.add, {
        dataflowId
      }),
      data: {
        dataProviderId,
        providerAccount
      },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
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

  deleteById: async representativeId => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.delete, {
        representativeId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  update: async (representativeId, providerAccount, dataProviderId) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.update, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        dataProviderId,
        id: representativeId,
        providerAccount
      }
    });
    return response;
  },

  updateProviderAccount: async (representativeId, providerAccount) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.update, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        id: representativeId,
        providerAccount
      }
    });
    return response;
  },

  updateProviderAccount: async (representativeId, dataProviderId) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.update, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        dataProviderId,
        id: representativeId
      }
    });
    return response;
  }
};

export { apiRepresentative };
