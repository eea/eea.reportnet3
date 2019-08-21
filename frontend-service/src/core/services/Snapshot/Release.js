export const Release = ({ snapshotRepository }) => async (dataFlowId, dataSetId, snapshotId) =>
  snapshotRepository.releaseById(dataFlowId, dataSetId, snapshotId);
