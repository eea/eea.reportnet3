import { ReleaseConfig } from 'conf/domain/model/Release/index';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiRelease = {
  allHistoricReleases: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(ReleaseConfig.loadAllHistoricReleases, {
        datasetId: datasetId
      })
    });
    return response.data;
  },

  allRepresentativeHistoricReleases: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(ReleaseConfig.loadAllRepresentativeHistoricReleases, {
        dataflowId: dataflowId,
        representativeId: dataProviderId
      })
    });
    return response.data;
  }
};
