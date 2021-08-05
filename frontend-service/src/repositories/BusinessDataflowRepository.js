import { BusinessDataflowConfig } from './config/BusinessDataflowConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const businessDataflowRepository = {
  all: async () => await HTTPRequester.get({ url: getUrl(BusinessDataflowConfig.all) }),

  create: async (name, description, obligationId, groupCompaniesId, fmeUserId) => {
    return await HTTPRequester.post({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: { name, description, obligation: { obligationId }, type: 'BUSINESS', groupCompaniesId, fmeUserId }
    });
  },

  edit: async (dataflowId, description, obligationId, name, groupCompaniesId, fmeUserId) =>
    await HTTPRequester.update({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: {
        description,
        id: dataflowId,
        obligation: { obligationId },
        name,
        type: 'BUSINESS',
        groupCompaniesId,
        fmeUserId
      }
    })
};
