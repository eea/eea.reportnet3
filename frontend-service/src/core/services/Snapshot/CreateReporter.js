export const CreateReporter = ({ snapshotRepository }) => async (datasetId, description, isReleased = false) =>
  snapshotRepository.createByIdReporter(datasetId, description, isReleased);
