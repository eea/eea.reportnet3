export const Delete = ({ snapshotRepository }) => async (dataSetId, snapshotId) =>
  snapshotRepository.deleteById(dataSetId, snapshotId);
