export const Restore = ({ snapshotRepository }) => async (dataFlowId, dataSetId, snapshotId) =>
  snapshotRepository.restoreById(dataFlowId, dataSetId, snapshotId);
