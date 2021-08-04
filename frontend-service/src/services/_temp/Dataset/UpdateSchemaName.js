export const UpdateSchemaName = ({ datasetRepository }) => async (datasetId, datasetSchemaName) =>
  datasetRepository.updateSchemaNameById(datasetId, datasetSchemaName);
