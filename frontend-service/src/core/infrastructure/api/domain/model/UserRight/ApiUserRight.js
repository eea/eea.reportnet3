import { UserRightConfig } from 'conf/domain/model/UserRight';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiUserRight = {
  allRequesters: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(UserRightConfig.allRequesters, { dataflowId })
    });

    return response;
  },

  allReporters: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(UserRightConfig.allReporters, { dataflowId, dataProviderId })
    });

    return response;
  },

  deleteRequester: async (account, dataflowId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteRequester, { dataflowId }),
      data: { account }
    });

    return response;
  },

  deleteReporter: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteReporter, { dataflowId, dataProviderId })
    });

    return response;
  },

  updateRequester: async (userRight, dataflowId) => {
    const response = await HTTPRequester.update({
      url: getUrl(UserRightConfig.updateRequester, { dataflowId }),
      data: {
        account: userRight.account,
        role: userRight.role
      }
    });
    return response;
  },

  updateReporter: async (userRight, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(UserRightConfig.updateReporter, { dataflowId, dataProviderId, role: userRight.role })
    });
    return response;
  }
};

export { apiUserRight };
