export const Create = ({ snapshotRepository }) => async (dataSetId, description) =>
  snapshotRepository.createById(dataSetId, description);
