import { BusinessDataflowConfig } from 'conf/domain/model/BusinessDataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiBusinessDataflow = {
  all: async () => await HTTPRequester.get({ url: getUrl(BusinessDataflowConfig.all) }),

  create: async (name, description, type) => {
    return await HTTPRequester.post({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: { name, description, type }
    });
  },

  edit: async (dataflowId, description, name, type) =>
    await HTTPRequester.update({
      url: getUrl(BusinessDataflowConfig.createDataflow),
      data: { description, id: dataflowId, name, type }
    })
};
