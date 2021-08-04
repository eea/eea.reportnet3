export const UpdateDatasetSchemaDesign = ({ datasetRepository }) => async (datasetId, datasetSchema) =>
  datasetRepository.updateDatasetSchemaDesign(datasetId, datasetSchema);
