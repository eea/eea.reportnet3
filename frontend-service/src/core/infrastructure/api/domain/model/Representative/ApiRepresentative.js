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

  allDataProviders: async dataProviderGroupId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allDataProviders, {
        dataProviderGroupId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  allRepresentatives: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allRepresentatives, {
        dataflowId: dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  deleteById: async representativeId => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteById, {
        representativeId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  getProviderTypes: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.getProviderTypes, {}),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  updateProviderAccount: async (representativeId, providerAccount) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateProviderAccount, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        id: representativeId,
        providerAccount: providerAccount
      }
    });
    return response;
  },

  updateDataProviderId: async (representativeId, dataProviderId) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateDataProviderId, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        id: representativeId,
        dataProviderId: dataProviderId
      }
    });
    return response;
  }
};

export { apiRepresentative };
