import { UserRightConfig } from './config/UserRightConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const UserRightRepository = {
  getRequesters: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(UserRightConfig.getRequesters, { dataflowId }) }),

  getReporters: async (dataflowId, dataProviderId) =>
    await HTTPRequester.get({ url: getUrl(UserRightConfig.getReporters, { dataflowId, dataProviderId }) }),

  deleteRequester: async (userRight, dataflowId) =>
    await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteRequester, { dataflowId }),
      data: { account: userRight.account, role: userRight.role }
    }),

  deleteReporter: async (userRight, dataflowId, dataProviderId) =>
    await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteReporter, { dataflowId, dataProviderId }),
      data: { account: userRight.account, role: userRight.role }
    }),

  updateRequester: async (userRight, dataflowId) =>
    await HTTPRequester.update({
      url: getUrl(UserRightConfig.updateRequester, { dataflowId }),
      data: { account: userRight.account, role: userRight.role }
    }),

  updateReporter: async (userRight, dataflowId, dataProviderId) =>
    await HTTPRequester.update({
      url: getUrl(UserRightConfig.updateReporter, { dataflowId, dataProviderId }),
      data: { account: userRight.account, role: userRight.role }
    })
};
