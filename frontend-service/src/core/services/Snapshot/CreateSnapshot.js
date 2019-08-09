export const CreateSnapshot = ({ snapshotRepository }) => async (dataSetId, description) =>
  snapshotRepository.createById(dataSetId, description);
