import { ReleaseConfig } from 'conf/domain/model/Release/index';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiRelease = {
  allDataCollectionHistoricReleases: async datasetId => {
    console.log('datasetId', datasetId);
    const response = await HTTPRequester.get({
      url: getUrl(ReleaseConfig.loadDataCollectionHistoricReleases, {
        datasetId: datasetId
      })
    });
    console.log('response', response);
    return response.data;
  }
};
