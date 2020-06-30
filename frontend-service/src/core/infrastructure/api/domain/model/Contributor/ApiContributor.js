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

  all: async dataflowId => {
    console.log('dataflowId', dataflowId);
    console.log(
      'url',
      getUrl(ContributorConfig.all, {
        dataflowId
      })
    );
    const response = await HTTPRequester.get({
      url: getUrl(ContributorConfig.all, {
        dataflowId
      })
    });
    console.log('response', response);
    return response.data;
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
