import { BusinessDataflowConfig } from 'conf/domain/model/BusinessDataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiBusinessDataflow = {
  all: async () => await HTTPRequester.get({ url: getUrl(BusinessDataflowConfig.all) }),

  create: async (name, description, obligationId, type, groupCompaniesId, fmeUserId) => {
    return await HTTPRequester.post({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: { name, description, obligationId, type, groupCompaniesId, fmeUserId }
    });
  },

  edit: async (dataflowId, description, name, type, groupCompaniesId, fmeUserId) =>
    await HTTPRequester.update({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: { description, id: dataflowId, name, type, groupCompaniesId, fmeUserId }
    })
};
