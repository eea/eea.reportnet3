import { CitizenScienceDataflowConfig } from './config/CitizenScienceDataflowConfig';
import { DataflowConfig } from './config/DataflowConfig';

import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const CitizenScienceDataflowRepository = {
  create: async (name, description, obligationId, type, bigData) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createUpdate),
      data: { name, description, obligation: { obligationId }, releasable: true, type: 'CITIZEN_SCIENCE', bigData }
    }),

  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) =>
    await HTTPRequester.post({
      url: getUrl(CitizenScienceDataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    }),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo, bigData) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        bigData,
        description,
        id: dataflowId,
        name,
        obligation: { obligationId },
        releasable: isReleasable,
        showPublicInfo,
        type: 'CITIZEN_SCIENCE'
      }
    })
};
