import { BusinessDataflowConfig } from './config/BusinessDataflowConfig';
import { DataflowConfig } from './config/DataflowConfig';

import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const BusinessDataflowRepository = {
  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        name,
        description,
        obligation: { obligationId },
        releasable: true,
        type: 'BUSINESS',
        dataProviderGroupId,
        fmeUserId
      }
    }),

  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) =>
    await HTTPRequester.post({
      url: getUrl(BusinessDataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    }),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        description,
        id: dataflowId,
        obligation: { obligationId },
        name,
        type: 'BUSINESS',
        dataProviderGroupId,
        fmeUserId
      }
    })
};
