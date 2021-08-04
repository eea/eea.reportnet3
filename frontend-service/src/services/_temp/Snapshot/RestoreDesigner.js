export const RestoreDesigner = ({ snapshotRepository }) => async (datasetSchemaId, snapshotId) =>
  snapshotRepository.restoreByIdDesigner(datasetSchemaId, snapshotId);
