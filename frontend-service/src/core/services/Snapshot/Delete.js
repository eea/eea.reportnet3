export const Delete = ({ snapshotRepository }) => async (datasetId, snapshotId) =>
  snapshotRepository.deleteById(datasetId, snapshotId);
