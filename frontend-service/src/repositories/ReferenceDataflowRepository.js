import { ReferenceDataflowConfig } from './config/ReferenceDataflowConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ReferenceDataflowRepository = {
  getAll: async () => await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.getAll) }),

  create: async (name, description, type) => {
    return await HTTPRequester.post({
      url: getUrl(ReferenceDataflowConfig.createUpdateReferenceDataflow),
      data: { name, description, type }
    });
  },

  update: async (dataflowId, description, name, type) =>
    await HTTPRequester.update({
      url: getUrl(ReferenceDataflowConfig.createUpdateReferenceDataflow),
      data: { description, id: dataflowId, name, type }
    }),

  getReferencingDataflows: async referenceDataflowId =>
    await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.getReferencingDataflows, { referenceDataflowId }) }),

  getReferenceDataflow: async referenceDataflowId =>
    await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.getReferenceDataflow, { referenceDataflowId }) })
};
