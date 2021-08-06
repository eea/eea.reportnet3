import { BusinessDataflowConfig } from './config/BusinessDataflowConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const BusinessDataflowRepository = {
  all: async () => await HTTPRequester.get({ url: getUrl(BusinessDataflowConfig.all) }),

  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) => {
    return await HTTPRequester.post({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: {
        name,
        description,
        obligation: { obligationId },
        type: 'BUSINESS',
        dataProviderGroupId,
        fmeUserId
      }
    });
  },

  edit: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    await HTTPRequester.update({
      url: getUrl(BusinessDataflowConfig.createDataflow),
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
