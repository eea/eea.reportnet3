export const Restore = ({ snapshotRepository }) => async (dataflowId, dataSetId, snapshotId) =>
  snapshotRepository.restoreById(dataflowId, dataSetId, snapshotId);
