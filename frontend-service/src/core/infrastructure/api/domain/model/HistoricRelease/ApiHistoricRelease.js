import { HistoricReleaseConfig } from 'conf/domain/model/HistoricRelease';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiHistoricRelease = {
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
