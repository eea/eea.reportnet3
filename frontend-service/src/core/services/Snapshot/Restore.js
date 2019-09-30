export const Restore = ({ snapshotRepository }) => async (dataflowId, datasetId, snapshotId) =>
  snapshotRepository.restoreById(dataflowId, datasetId, snapshotId);
