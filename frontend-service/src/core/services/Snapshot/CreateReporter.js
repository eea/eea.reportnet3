export const CreateReporter = ({ snapshotRepository }) => async (datasetId, description, isRelease = false) =>
  snapshotRepository.createByIdReporter(datasetId, description, isRelease);
