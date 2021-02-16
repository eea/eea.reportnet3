import { RepresentativeConfig } from 'conf/domain/model/Representative';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiRepresentative = {
  add: async (dataflowId, providerAccount, dataProviderId) => {
    const response = await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.add, {
        dataflowId
      }),
      data: {
        dataProviderId,
        providerAccounts: [providerAccount]
      }
    });
    return response;
  },

  allDataProviders: async dataProviderGroupId => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allDataProviders, {
        dataProviderGroupId
      })
    });
    return response;
  },

  allRepresentatives: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allRepresentatives, {
        dataflowId: dataflowId
      })
    });
    return response;
  },

  deleteById: async representativeId => {
    const response = await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteById, {
        representativeId
      })
    });

    return response;
  },

  getProviderTypes: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.getProviderTypes, {})
    });
    return response;
  },

  updateProviderAccount: async (representativeId, providerAccount) => {
    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateProviderAccount, {}),
      data: {
        id: representativeId,
        providerAccounts: [providerAccount]
      }
    });
    return response;
  },

  updateDataProviderId: async (representativeId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateDataProviderId, {}),
      data: {
        id: representativeId,
        dataProviderId: dataProviderId
      }
    });
    return response;
  }
};

export { apiRepresentative };
