import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { ControlStatusesConfig } from './config/ControlStatusesConfig';

export const ControlStatusesRepository = {
  getDatasetData: async (datasetId, dataProviderId) =>
    await HTTPRequester.post({
      url: getUrl(ControlStatusesConfig.getDatasetData, {
        datasetId,
        dataProviderId
      })
    }),

  deleteDatasetData: async datasetId =>
    await HTTPRequester.delete({
      url: getUrl(ControlStatusesConfig.deleteDatasetData, {
        datasetId
      })
    })
};
