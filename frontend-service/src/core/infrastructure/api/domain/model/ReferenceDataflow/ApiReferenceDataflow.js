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

  edit: async (dataflowId, description, name, type) =>
    await HTTPRequester.update({
      url: getUrl(ReferenceDataflowConfig.createDataflow),
      data: { description, id: dataflowId, name, type }
    }),

  deleteReferenceDataflow: async referenceDataflowId =>
    await HTTPRequester.delete({
      url: getUrl(ReferenceDataflowConfig.deleteReferenceDataflow, { referenceDataflowId })
    }),

  getReferencingDataflows: async referenceDataflowId =>
    await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.getReferencingDataflows, { referenceDataflowId }) }),

  referenceDataflow: async referenceDataflowId =>
    await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.referenceDataflow, { referenceDataflowId }) })
};
