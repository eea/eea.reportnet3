import { HistoricReleaseConfig } from 'conf/domain/model/HistoricRelease';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiHistoricRelease = {
  allHistoricReleases: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(HistoricReleaseConfig.loadAllHistoricReleases, {
        datasetId
      })
    });

    return response.data;
  },

  allRepresentativeHistoricReleases: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(HistoricReleaseConfig.loadAllRepresentativeHistoricReleases, {
        dataflowId,
        representativeId: dataProviderId
      })
    });

    return response.data;
  }
};
