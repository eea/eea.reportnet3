import { CitizenScienceDataflowConfig } from './config/CitizenScienceDataflowConfig';
import { DataflowConfig } from './config/DataflowConfig';

import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const CitizenScienceDataflowRepository = {
  create: async (name, description, obligationId, type, bigData, dataProviderGroupId) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        name,
        description,
        obligation: { obligationId },
        type: 'CITIZEN_SCIENCE',
        bigData,
        dataProviderGroupId,
        releasable: true
      }
    }),

  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) =>
    await HTTPRequester.post({
      url: getUrl(CitizenScienceDataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    }),

  update: async (
    dataflowId,
    name,
    description,
    obligationId,
    isReleasable,
    showPublicInfo,
    bigData,
    dataProviderGroupId,
    deadlineDate
  ) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        id: dataflowId,
        name,
        description,
        obligation: { obligationId },
        releasable: isReleasable,
        showPublicInfo,
        bigData,
        dataProviderGroupId,
        deadlineDate,
        type: 'CITIZEN_SCIENCE'
      }
    })
};
