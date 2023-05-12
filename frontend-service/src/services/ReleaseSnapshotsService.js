import { ReleaseSnapshotsRepository } from 'repositories/ReleaseSnapshotsRepository';

export const ReleaseSnapshotsService = {
  getLatestReleaseSnapshots: async (dataflowId, datasetId) => {
    const releaseSnapshotsData = await ReleaseSnapshotsRepository.getLatestReleaseSnapshots(dataflowId, datasetId);

    return releaseSnapshotsData;
  },

  downloadSnapshot: async (datasetId, dataflowId, fileName) =>
    await ReleaseSnapshotsRepository.downloadSnapshot(datasetId, dataflowId, fileName)
};
