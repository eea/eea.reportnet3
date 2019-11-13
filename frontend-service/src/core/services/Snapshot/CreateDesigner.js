export const CreateDesigner = ({ snapshotRepository }) => async (datasetId, datasetSchemaId, description) =>
  snapshotRepository.createByIdDesigner(datasetId, datasetSchemaId, description);
