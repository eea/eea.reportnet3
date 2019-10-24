export const DeleteSchema = ({ datasetRepository }) => async (datasetId, datasetSchemaId) =>
  datasetRepository.deleteSchemaById(datasetId, datasetSchemaId);
