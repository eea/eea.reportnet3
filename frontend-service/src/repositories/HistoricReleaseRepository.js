import { HistoricReleaseConfig } from './config/HistoricReleaseConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const HistoricReleaseRepository = {
  getAll: async datasetId => await HTTPRequester.get({ url: getUrl(HistoricReleaseConfig.getAll, { datasetId }) }),

  getAllRepresentative: async (dataflowId, dataProviderId) =>
    await HTTPRequester.get({
      url: getUrl(HistoricReleaseConfig.getAllRepresentative, {
        dataflowId,
        representativeId: dataProviderId
      })
    })
};
