export const CreateReporter = ({ snapshotRepository }) => async (datasetId, description) =>
  snapshotRepository.createByIdReporter(datasetId, description);
