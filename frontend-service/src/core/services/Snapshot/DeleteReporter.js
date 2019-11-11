export const DeleteReporter = ({ snapshotRepository }) => async (datasetId, snapshotId) =>
  snapshotRepository.deleteByIdReporter(datasetId, snapshotId);
