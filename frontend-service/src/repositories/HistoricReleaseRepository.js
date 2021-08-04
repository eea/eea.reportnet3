import { HistoricReleaseConfig } from './config/HistoricReleaseConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const historicReleaseRepository = {
  allHistoricReleases: async datasetId => {
    return await HTTPRequester.get({ url: getUrl(HistoricReleaseConfig.loadAllHistoricReleases, { datasetId }) });
  },

  allRepresentativeHistoricReleases: async (dataflowId, dataProviderId) => {
    return await HTTPRequester.get({
      url: getUrl(HistoricReleaseConfig.loadAllRepresentativeHistoricReleases, {
        dataflowId,
        representativeId: dataProviderId
      })
    });
  }
};
