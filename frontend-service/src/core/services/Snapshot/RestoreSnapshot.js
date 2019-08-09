export const RestoreSnapshot = ({ snapshotRepository }) => async (dataFlowId, dataSetId, snapshotId) =>
  snapshotRepository.restoreById(dataFlowId, dataSetId, snapshotId);
