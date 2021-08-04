export const DeleteDesigner = ({ snapshotRepository }) => async (datasetSchemaId, snapshotId) =>
  snapshotRepository.deleteByIdDesigner(datasetSchemaId, snapshotId);
