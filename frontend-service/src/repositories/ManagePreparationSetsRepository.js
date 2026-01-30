import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { ManagePreparationSetsConfig } from './config/ManagePreparationSetsConfig';

export const ManagePreparationSetsRepository = {
  getPreparationSets: async ({ dataflowId, providerId }) => {
    return await HTTPRequester.get({
      url: getUrl(ManagePreparationSetsConfig.getPreparationSets, {
        dataflowId,
        providerId
      })
    });
  },

  createPreparationSet: async ({ datasetName, code, dataflowId, providerId, isCreated }) => {
    return await HTTPRequester.post({
      url: getUrl(ManagePreparationSetsConfig.createPreparationSet),
      data: { datasetName, code, dataflowId, providerId, isCreated }
    });
  },

  deletePreparationSet: async ({ id }) =>
    await HTTPRequester.delete({
      url: getUrl(ManagePreparationSetsConfig.deletePreparationSet, { id })
    })
};
