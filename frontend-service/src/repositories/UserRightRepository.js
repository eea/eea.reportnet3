import { UserRightConfig } from './config/UserRightConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const UserRightRepository = {
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

  deleteRequester: async (userRight, dataflowId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteRequester, { dataflowId }),
      data: { account: userRight.account, role: userRight.role }
    });

    return response;
  },

  deleteReporter: async (userRight, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteReporter, { dataflowId, dataProviderId }),
      data: { account: userRight.account, role: userRight.role }
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
      url: getUrl(UserRightConfig.updateReporter, { dataflowId, dataProviderId }),
      data: {
        account: userRight.account,
        role: userRight.role
      }
    });
    return response;
  }
};
