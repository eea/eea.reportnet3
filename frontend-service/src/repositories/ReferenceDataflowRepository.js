import { DataflowConfig } from './config/DataflowConfig';
import { ReferenceDataflowConfig } from './config/ReferenceDataflowConfig';

import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ReferenceDataflowRepository = {
  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) =>
    await HTTPRequester.get({
      url: getUrl(ReferenceDataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    }),

  create: async (name, description, type) =>
    await HTTPRequester.post({ url: getUrl(DataflowConfig.createUpdate), data: { name, description, type } }),

  update: async (dataflowId, description, name, type) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: { description, id: dataflowId, name, type }
    }),

  getReferencingDataflows: async referenceDataflowId =>
    await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.getReferencingDataflows, { referenceDataflowId }) })
};
