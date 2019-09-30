export const Create = ({ snapshotRepository }) => async (datasetId, description) =>
  snapshotRepository.createById(datasetId, description);
