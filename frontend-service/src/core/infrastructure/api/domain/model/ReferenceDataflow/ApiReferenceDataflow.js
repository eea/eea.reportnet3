import { ReferenceDataflowConfig } from 'conf/domain/model/ReferenceDataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiReferenceDataflow = {
  all: async () => await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.all) }),

  create: async (name, description, type) => {
    return await HTTPRequester.post({
      url: getUrl(ReferenceDataflowConfig.createDataflow),
      data: { name, description, type }
    });
  },

  referenceDataflow: async () => await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.referenceDataflow) })
};
