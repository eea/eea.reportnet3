export const Release = ({ snapshotRepository }) => async (dataflowId, dataSetId, snapshotId) =>
  snapshotRepository.releaseById(dataflowId, dataSetId, snapshotId);
