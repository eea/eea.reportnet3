import { BusinessDataflowConfig } from './config/BusinessDataflowConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const BusinessDataflowRepository = {
  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    await HTTPRequester.post({
      url: getUrl(BusinessDataflowConfig.createUpdateReferenceDataflow),
      data: { name, description, obligation: { obligationId }, type: 'BUSINESS', dataProviderGroupId, fmeUserId }
    }),

  delete: async dataflowId =>
    await HTTPRequester.delete({
      url: getUrl(BusinessDataflowConfig.delete, { dataflowId })
    }),

  getAll: async () => await HTTPRequester.get({ url: getUrl(BusinessDataflowConfig.getAll) }),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    await HTTPRequester.update({
      url: getUrl(BusinessDataflowConfig.createUpdateReferenceDataflow),
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
