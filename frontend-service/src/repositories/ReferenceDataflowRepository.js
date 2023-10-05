import { DataflowConfig } from './config/DataflowConfig';
import { ReferenceDataflowConfig } from './config/ReferenceDataflowConfig';

import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ReferenceDataflowRepository = {
  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) =>
    await HTTPRequester.post({
      url: getUrl(ReferenceDataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    }),

  create: async (name, description, type, bigData) =>
    await HTTPRequester.post({ url: getUrl(DataflowConfig.createUpdate), data: { name, description, type, bigData } }),

  update: async (dataflowId, description, name, type, bigData) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: { description, id: dataflowId, name, type, bigData }
    }),

  getReferencingDataflows: async referenceDataflowId =>
    await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.getReferencingDataflows, { referenceDataflowId }) })
};
