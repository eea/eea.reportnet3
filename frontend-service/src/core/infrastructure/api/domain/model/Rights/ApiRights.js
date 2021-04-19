import { RightsConfig } from 'conf/domain/model/Rights';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiRights = {
  allEditors: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(RightsConfig.allEditors, { dataflowId })
    });

    return response;
  },

  allRequesters: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(RightsConfig.allRequesters, { dataflowId, dataProviderId })
    });

    return response;
  },

  allReporters: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(RightsConfig.allReporters, { dataflowId, dataProviderId })
    });

    return response;
  },

  deleteEditor: async (account, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(RightsConfig.deleteEditor, { dataflowId }),
      data: { account }
    });

    return response;
  },

  deleteRequester: async (account, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(RightsConfig.deleteRequester, { dataflowId }),
      data: { account }
    });

    return response;
  },

  deleteReporter: async (account, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(RightsConfig.deleteReporter, { dataflowId, dataProviderId })
    });

    return response;
  },

  updateEditor: async (user, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(RightsConfig.updateEditor, { dataflowId }),
      data: {
        account: user.account,
        writePermission: user.writePermission
      }
    });
    return response;
  },

  updateRequester: async (user, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(RightsConfig.updateRequester, { dataflowId }),
      data: {
        account: user.account,
        writePermission: user.writePermission
      }
    });
    return response;
  },

  updateReporter: async (user, dataflowId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(RightsConfig.updateReporter, { dataflowId, dataProviderId })
    });
    return response;
  }
};

export { apiRights };
