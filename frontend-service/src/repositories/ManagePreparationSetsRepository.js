import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { ManagePreparationSetsConfig } from './config/ManagePreparationSetsConfig';

export const ManagePreparationSetsRepository = {
  addPreparationSet: async ({ datasetName, code, dataflowId, providerId, isCreated }) => {
    return await HTTPRequester.post({
      url: getUrl(ManagePreparationSetsConfig.addPreparationSet),
      data: { datasetName, code, dataflowId, providerId, isCreated }
    });
  },

  createPreparationSets: async ({ dataflowId, providerId }) =>
    await HTTPRequester.post({
      url: getUrl(ManagePreparationSetsConfig.createPreparationSets, { dataflowId, providerId })
    }),

  deletePreparationSet: async ({ id }) =>
    await HTTPRequester.delete({
      url: getUrl(ManagePreparationSetsConfig.deletePreparationSet, { id })
    }),

  getPreparationSets: async ({ dataflowId, providerId, code }) => {
    return await HTTPRequester.get({
      url: getUrl(ManagePreparationSetsConfig.getPreparationSets, {
        dataflowId,
        providerId,
        code
      })
    });
  }
};
