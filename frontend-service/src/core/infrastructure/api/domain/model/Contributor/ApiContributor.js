import { ContributorConfig } from 'conf/domain/model/Contributor';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const apiContributor = {
  all: async (dataflowId, dataProviderId = null) => {
    const isReporter = !isEmpty(dataProviderId.toString());
    console.log(isReporter ? 'miguel.provider' : 'miguel.custodian');

    const response = await HTTPRequester.get({
      url: isReporter
        ? getUrl(ContributorConfig.allReporters, {
            dataflowId,
            dataProviderId
          })
        : getUrl(ContributorConfig.allEditors, {
            dataflowId
          })
    });

    return response.data;
  },

  delete: async (account, dataflowId, dataProviderId = null) => {
    const isReporter = !isEmpty(dataProviderId.toString());
    console.log('dataProviderId', dataProviderId);

    const response = await HTTPRequester.delete({
      url: isReporter
        ? getUrl(ContributorConfig.deleteReporter, { dataflowId, dataProviderId })
        : getUrl(ContributorConfig.deleteEditor, { dataflowId }),
      data: {
        account: account
      }
    });

    return response;
  },

  update: async (contributor, dataflowId, dataProviderId = null) => {
    const isReporter = !isEmpty(dataProviderId.toString());
    console.log(isReporter ? 'miguel.provider' : 'custodian');

    const response = await HTTPRequester.update({
      url: isReporter
        ? getUrl(ContributorConfig.updateReporter, { dataflowId, dataProviderId })
        : getUrl(ContributorConfig.updateEditor, { dataflowId }),
      data: {
        account: contributor.account,
        writePermission: Boolean(contributor.writePermission)
      }
    });
    return response;
  }
};

export { apiContributor };
