export const Release = ({ snapshotRepository }) => async (dataflowId, datasetId, snapshotId) =>
  snapshotRepository.releaseById(dataflowId, datasetId, snapshotId);
