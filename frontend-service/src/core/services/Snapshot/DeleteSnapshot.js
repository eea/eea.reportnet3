export const DeleteSnapshot = ({ snapshotRepository }) => async (dataSetId, snapshotId) =>
  snapshotRepository.deleteById(dataSetId, snapshotId);
