export const ReleaseDesigner = ({ snapshotRepository }) => async (datasetSchemaId, snapshotId) =>
  snapshotRepository.releaseByIdDesigner(datasetSchemaId, snapshotId);
