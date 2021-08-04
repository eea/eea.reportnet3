export const RestoreReporter = ({ snapshotRepository }) => async (dataflowId, datasetId, snapshotId) =>
  snapshotRepository.restoreByIdReporter(dataflowId, datasetId, snapshotId);
