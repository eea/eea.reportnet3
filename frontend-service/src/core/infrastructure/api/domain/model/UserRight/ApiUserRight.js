import { UserRightConfig } from 'conf/domain/model/UserRight';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiUserRight = {
  allEditors: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(UserRightConfig.allEditors, { dataflowId })
    });

    return response;
  },

  allRequesters: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(UserRightConfig.allEditors, { dataflowId })
    });

    return response;
  },

  allReporters: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(UserRightConfig.allReporters, { dataflowId, dataProviderId })
    });

    return response;
  },

  deleteEditor: async (account, dataflowId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(UserRightConfig.deleteEditor, { dataflowId }),
      data: { account }
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

  updateEditor: async (userRight, dataflowId) => {
    const response = await HTTPRequester.update({
      url: getUrl(UserRightConfig.updateEditor, { dataflowId }),
      data: {
        account: userRight.account,
        writePermission: userRight.writePermission
      }
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

  updateReporter: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(UserRightConfig.updateReporter, { dataflowId, dataProviderId })
    });
    return response;
  }
};

export { apiUserRight };
