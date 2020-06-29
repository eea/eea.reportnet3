import { ContributorConfig } from 'conf/domain/model/Contributor';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiContributor = {
  add: async (Contributor, dataflowId) => {
    const response = await HTTPRequester.post({
      url: getUrl(ContributorConfig.addEditor, {
        dataflowId
      }),
      data: {
        account: Contributor.account,
        dataProviderId: Contributor.dataProviderId,
        writePermission: Contributor.writePermission
      }
    });
    return response;
  },

  all: async (dataflowId, dataProviderId, userId) => {
    const response = await HTTPRequester.get({
      url: getUrl(ContributorConfig.all, {
        dataflowId
      }),
      data: {
        userId,
        dataProviderId
      }
    });
    return response;
  },

  deleteContributor: async (Contributor, dataflowId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(ContributorConfig.delete, { dataflowId }),
      data: {
        account: Contributor.account,
        dataProviderId: Contributor.dataProviderId,
        writePermission: Contributor.writePermission
      }
    });

    return response;
  },
  updateWritePermission: async (Contributor, dataflowId) => {
    const response = await HTTPRequester.update({
      url: getUrl(ContributorConfig.updateWritePermission, { dataflowId }),
      data: {
        account: Contributor.account,
        dataProviderId: Contributor.dataProviderId,
        writePermission: Contributor.writePermission
      }
    });
    return response;
  }
};

export { apiContributor };
