import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { ReleaseSnapshotsConfig } from './config/ReleaseSnapshotsConfig';

export const ReleaseSnapshotsRepository = {
  getLatestReleaseSnapshots: async (dataflowId, datasetId) =>
    await HTTPRequester.get({
      url: getUrl(ReleaseSnapshotsConfig.getLatestReleaseSnapshots, { dataflowId, datasetId })
    }),

  downloadSnapshot: async (datasetId, dataflowId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(ReleaseSnapshotsConfig.downloadSnapshot, { datasetId, dataflowId, fileName })
    })
};
