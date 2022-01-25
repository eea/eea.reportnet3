import { CitizenScienceDataflowConfig } from './config/CitizenScienceDataflowConfig';
import { DataflowConfig } from './config/DataflowConfig';

import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const CitizenScienceDataflowRepository = {
  create: async (name, description, obligationId) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createUpdate),
      data: { name, description, obligation: { obligationId }, releasable: true, type: 'CITIZEN_SCIENCE' }
    }),

  getAll: async ({ filterBy, isAscending, pageNumber, pageSize, sortBy }) =>
    await HTTPRequester.get({
      url: getUrl(CitizenScienceDataflowConfig.getAll, { isAscending, pageNumber, pageSize, sortBy }),
      data: { ...filterBy }
    }),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
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
