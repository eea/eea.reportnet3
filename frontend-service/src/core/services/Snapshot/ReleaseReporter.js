export const ReleaseReporter = ({ snapshotRepository }) => async (dataflowId, datasetId, snapshotId) =>
  snapshotRepository.releaseByIdReporter(dataflowId, datasetId, snapshotId);
